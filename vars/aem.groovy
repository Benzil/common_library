// Config provider plugin needed
// Config should be in JSON format, example could be found in the root of this repository
def calculateConfig(environment) {
  configFileProvider([configFile(fileId: 'web_config', variable: 'CONFIG')]){
    def conf = readJSON file: "${CONFIG}"
    
    def configObject = [:]
    configObject["global"] = conf.global
    try {
      configObject["authors"] = conf["stacks"]["${environment}"]["authors"]
      configObject["publishers"] = conf["stacks"]["${environment}"]["publishers"]
      configObject["dispatchers"] = conf["stacks"]["${environment}"]["dispatchers"]
      log.printMagenta("[INFO] Found ${environment} stack")
    } catch(Exception ex) {
      log.printRed("[ERROR] Can't find provided environment")
      log.printRed("[ERROR] Check if ${environment} is in a config and it has authors, publishers, dispatchers defined")
      currentBuild.result = 'FAILURE'
    }
    return configObject
  }
}

def calculateConfig() {
  configFileProvider([configFile(fileId: 'web_config', variable: 'CONFIG')]){
    def conf = readJSON file: "${CONFIG}"
    
    def configObject = [:]
    configObject["global"] = conf.global
    return configObject
  }
}

// Returns list of all authors and publishers
def collectAemInstances(configObject) {
  def instances = []
  instances.addAll(configObject.authors)
  instances.addAll(configObject.publishers)
  return instances
}

def invalidateCache(configObject) {
  configObject.dispatchers.each {dispatcher ->
    log.printMagenta("[INFO] Invalidating cache on ${dispatcher}")
    sh(script: "curl -H 'CQ-Action: Delete' -H 'CQ-Handle: /' -H 'CQ-Path: /' -H 'Content-Length:0' -H 'Content-Type: application/octet-stream' --noproxy .com http://${dispatcher}/invalidate.cache")
  }
}

def flushJsp(configObject) {
  instances = collectAemInstances(configObject)
  withCredentials([usernameColonPassword(credentialsId: configObject.global.aem_admin_id, variable: 'admin')]){
    instances.each {instance ->
      log.printMagenta("[INFO] Sending cURL to slingjsp on ${instance}")
      sh(script: "curl -u ${admin} -I -X POST http://${instance}/system/console/slingjsp")
    }
  }
}

def deployBundle(creds, bundle, instance) {
  log.printMagenta("[INFO] Deploying ${bundle} on ${instance}")
  try {
    sh(script: "curl -u ${creds} -F action=install -F bundlestartlevel=20 -F bundlefile=@${bundle} http://${instance}/system/console/bundles")
    sh(script: "curl -u ${creds} -F action=start http://${instance}/system/console/bundles/com.upc.day.core.orion-core")
    log.printMagenta("[INFO] Bundle deployed successfully")
  } catch (Exception ex) {
    log.printRed("[ERROR] Deployment failed")
    log.printRed("[ERROR] ${ex}")
    currentBuild.result = 'FAILURE'
  }
}

def deployPackage(creds, pack, instance) {
  log.printMagenta("[INFO] Deploying ${package} on ${instance}")
  try {
    sh(script: "curl -u ${creds} -X POST -F cmd=upload -F force=true -F package=@${pack} http://${instance}/crx/packmgr/service/.json")
    sh(script: "curl -u ${creds} -X POST -F cmd=install http://${instance}/crx/packmgr/service/.json/etc/packages/com.upc.day/${pack}")
    log.printMagenta("[INFO] Package deployed successfully")
  } catch (Exception ex) {
    log.printRed("[ERROR] Deployment failed")
    log.printRed("[ERROR] ${ex}")
    currentBuild.result = 'FAILURE'
  }
}

// Outputs whole list of bundles, output redirected to /dev/null
def refreshBundles(configObject) {
  instances = collectAemInstances(configObject)
  withCredentials([usernameColonPassword(credentialsId: configObject.global.aem_admin_id, variable: 'admin')]){
    instances.each {instance ->
      log.printMagenta("[INFO] Sending cURL to refresh bundles on ${instance}")
      sh(script: "curl -u ${admin} -X POST -F action=refreshPackages http://${instance}/system/console/bundles > /dev/null")
    }
  }
}

def deployArtifact(configObject) {
  dir('artifacts') {
    instances = collectAemInstances(configObject)
    withCredentials([usernameColonPassword(credentialsId: configObject.global.aem_admin_id, variable: 'admin')]) {
      sh(script: 'ls -la')
      instances.each { instance -> 
        try {
          orion_core = sh(returnStdout: true, script: "ls orion-core*.jar").trim()
          deploy_bundle(admin, orion_core, instance)
        } catch (Exception ex) {
          log.printRed("[ERROR] Bundle orion-core couldn't be found in current folder"
          log.printRed("[ERROR] ${ex}")
        }

        try {
          orion_config = sh(returnStdout: true, script: "ls orion-config*.zip").trim()
          deploy_package(admin, orion_config, instance)
        } catch (Exception ex) {
          log.printRed("[ERROR] Package orion-config couldn't be found in current folder"
          log.printRed("[ERROR] ${ex}")
        }

        try {
          orion_content = sh(returnStdout: true, script: "ls orion-content*.zip").trim()
          deploy_package(admin, orion_content, instance)
        } catch (Exception ex) {
          log.printRed("[ERROR] Package orion-content couldn't be found in current folder"
          log.printRed("[ERROR] ${ex}")
        }

        try {
          orion_chromecast = sh(returnStdout: true, script: "ls orion-chromecast*.zip").trim()
          deploy_package(admin, orion_chromecast, instance)
        } catch (Exception ex) {
          log.printRed("[ERROR] Package orion-chromecast couldn't be found in current folder"
          log.printRed("[ERROR] ${ex}")
        }
      }
    }
  }
}

def checkArtifact(configObject) {
}

// Build package which contains core, config, chromecast and content parts
def buildArtifact(configObject) {
  configFileProvider([configFile(fileId: 'maven_settings', variable: 'MAVEN_SETTINGS_XML')]){

    log.printMagenta("[INFO] Compiling orion-core")
    sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -DnewVersion=${configObject.global.version} -f ./orion-core/pom.xml clean versions:set versions:commit")
    sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -f ./orion-core/pom.xml package install -DskipTests")

    log.printMagenta("[INFO] Compiling orion-content")
    sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -DnewVersion=${configObject.global.version} -f ./orion-content/pom.xml clean versions:set versions:commit")
    sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -f ./orion-content/pom.xml package")

    log.printMagenta("[INFO] Compiling orion-config")
    sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -DnewVersion=${configObject.global.version} -f ./orion-config/pom.xml clean versions:set versions:commit")
    sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -f ./orion-config/pom.xml package -P config-default")

    log.printMagenta("[INFO] Compiling chromecast")
    sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -DnewVersion=${configObject.global.version} -f ./orion-chromecast-receiver/pom.xml clean versions:set versions:commit")
    sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -f ./orion-chromecast-receiver/pom.xml package -Doutput=chromecast")
  }
}

def packageArtifact(tag) {
  dir('artifacts'){
    def packages = ['orion-core', 'orion-content', 'orion-config', 'orion-chromecast-receiver']
    packages.each { pack ->
      try {
        log.printMagenta("[INFO] Copying ${pack} to artifacts folder")
        if (pack == 'orion-core') {
          sh(script: "cp ../${pack}/target/*.jar ./")
        } else {
          sh(script: "cp ../${pack}/target/*.zip ./")
        }
      } catch (Exception ex) {
        log.printRed("[ERROR] Package ${pack} could not be found")
      }
    }
    sh(script: "tar -cvzf ${tag}.tar.gz ./*")
  }
}

def checkoutTag(configObject, tag) {
  checkout scm: [
    $class: 'GitSCM',
    branches: [[name: "refs/tags/${tag}"]],
    doGenerateSubmoduleConfigurations: false,
    extensions: [],
    submoduleCfg: [], 
    userRemoteConfigs: [[
      credentialsId: configObject.global.git_user_id,
      url: configObject.global.repository
    ]]
  ]
}

def checkoutBranch(configObject, branch) {
  checkout scm: [
    $class: 'GitSCM',
    branches: [[name: "${branch}"]],
    doGenerateSubmoduleConfigurations: false,
    extensions: [],
    submoduleCfg: [], 
    userRemoteConfigs: [[
      credentialsId: configObject.global.git_user_id,
      url: configObject.global.repository
    ]]
  ]
}