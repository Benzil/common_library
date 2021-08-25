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

// Dublicates function calculateConfig but without providing any environments
def calculateConfig() {
  configFileProvider([configFile(fileId: 'web_config', variable: 'CONFIG')]){
    def conf = readJSON file: "${CONFIG}"
    
    def configObject = [:]
    configObject["global"] = conf.global
    return configObject
  }
}

// Returns list of all authors and publishers from provided config
def collectAemInstances(configObject) {
  def instances = []
  instances.addAll(configObject.authors)
  instances.addAll(configObject.publishers)
  return instances
}

// Checkout section
def checkoutTag(configObject, tag) {
  checkout scm: [
    $class: 'GitSCM',
    branches: [[name: "refs/tags/${tag}"]],
    doGenerateSubmoduleConfigurations: false,
    extensions: [[$class: 'CloneOption', timeout: 20]],
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
    extensions: [[$class: 'CloneOption', timeout: 20]],
    submoduleCfg: [], 
    userRemoteConfigs: [[
      credentialsId: configObject.global.git_user_id,
      url: configObject.global.repository
    ]]
  ]
}

// Generic functions to deploy bundle and package to AEM instance
def deployBundle(creds, bundle, instance) {
  log.printMagenta("[INFO] Deploying ${bundle} on ${instance}")
  try {
    sh(script: 'curl -u '+creds+' -F action=install -F bundlestartlevel=20 -F bundlefile=@'+bundle+' http://'+instance+'/system/console/bundles')
    sh(script: 'curl -u '+creds+' -F action=start http://'+instance+'/system/console/bundles/com.upc.day.core.orion-core')
    log.printMagenta("[INFO] Bundle deployed successfully")
  } catch (Exception ex) {
    log.printRed("[ERROR] Deployment failed")
    log.printRed("[ERROR] ${ex}")
    currentBuild.result = 'FAILURE'
  }
}

def deployPackage(creds, pack, instance) {
  log.printMagenta("[INFO] Deploying ${pack} on ${instance}")
  try {
    sh(script: "curl -u ${creds} -X POST -F force=true -F package=@${pack} http://${instance}/crx/packmgr/service/.json/?cmd=upload -vv")
    sh(script: "curl -u ${creds} -X POST http://${instance}/crx/packmgr/service/.json/etc/packages/com.upc.day/${pack}?cmd=install -vv")
    log.printMagenta("[INFO] Package deployed successfully")
  } catch (Exception ex) {
    log.printRed("[ERROR] Deployment failed")
    log.printRed("[ERROR] ${ex}")
    currentBuild.result = 'FAILURE'
  }
}

// Triggers bundles refresh, output redirected to /dev/null
def refreshBundles(configObject) {
  instances = collectAemInstances(configObject)
  withCredentials([usernameColonPassword(credentialsId: configObject.global.aem_admin_id, variable: 'admin')]){
    instances.each {instance ->
      log.printMagenta("[INFO] Sending cURL to refresh bundles on ${instance}")
      sh(script: 'curl -u '+admin+' -X POST -F action=refreshPackages http://'+instance+'/system/console/bundles > /dev/null')
    }
  }
}

// Build package which contains core, config, chromecast and content parts
def buildArtifact(version, build_config, build_content, build_chromecast) {
  configFileProvider([configFile(fileId: 'maven_settings', variable: 'MAVEN_SETTINGS_XML')]){

    log.printMagenta("[INFO] Compiling orion-core")
    sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -DnewVersion=${version} -f ./orion-core/pom.xml clean versions:set versions:commit")
    sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -f ./orion-core/pom.xml package install -DskipTests")

    if(build_config == true ) { 
      log.printMagenta("[INFO] Compiling orion-config")
      sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -DnewVersion=${version} -f ./orion-config/pom.xml clean versions:set versions:commit")
      sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -f ./orion-config/pom.xml package -P config-default")
    } else {
      log.printMagenta("[INFO] Skipping orion-config compilation")
    }

    if(build_content == true ) {
      log.printMagenta("[INFO] Compiling orion-content")
      sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -DnewVersion=${version} -f ./orion-content/pom.xml clean versions:set versions:commit")
      sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -f ./orion-content/pom.xml package")
    } else {
      log.printMagenta("[INFO] Skipping orion-content compilation")
    }

    if(build_chromecast == true) {
      log.printMagenta("[INFO] Compiling chromecast")
      sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -DnewVersion=${version} -f ./orion-chromecast-receiver/pom.xml clean versions:set versions:commit")
      sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -f ./orion-chromecast-receiver/pom.xml package")
    } else {
      log.printMagenta("[INFO] Skipping orion-chromecast compilation")
    }
  }
}

def buildConfig(version, configProfile) {
  configFileProvider([configFile(fileId: 'maven_settings', variable: 'MAVEN_SETTINGS_XML')]){
    log.printMagenta("[INFO] Compiling orion-core")
    sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -DnewVersion=${version} -f ./orion-core/pom.xml clean versions:set versions:commit")
    sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -f ./orion-core/pom.xml package install -DskipTests")

    log.printMagenta("[INFO] Compiling orion-config")
    sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -DnewVersion=${version} -f ./orion-config/pom.xml clean versions:set versions:commit")
    sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -f ./orion-config/pom.xml package -P ${configProfile}")
  }
}

def deployArtifact(configObject, deploy_core, deploy_config, deploy_content, deploy_chromecast) {
  dir('artifacts') {
    instances = collectAemInstances(configObject)
    withCredentials([usernameColonPassword(credentialsId: configObject.global.aem_admin_id, variable: 'admin')]) {
      sh(script: 'ls -la')
      instances.each { instance -> 
        if (deploy_core == true) {
          try {
            orion_core = sh(returnStdout: true, script: "ls orion-core*.jar").trim()
            deployBundle(admin, orion_core, instance)
          } catch (Exception ex) {
            log.printRed("[ERROR] Bundle orion-core couldn't be found in current folder")
            log.printRed("[ERROR] ${ex}")
          }
        } else {
          log.printMagenta("[INFO] Skipping core deployment")
        }

        if (deploy_config == true) {
          try {
            orion_config = sh(returnStdout: true, script: "ls orion-config*.zip").trim()
            deployPackage(admin, orion_config, instance)
          } catch (Exception ex) {
            log.printRed("[ERROR] Package orion-config couldn't be found in current folder")
            log.printRed("[ERROR] ${ex}")
          }
        } else {
          log.printMagenta("[INFO] Skipping config deployment")
        }

        if (deploy_content == true ) {
          try {
            orion_content = sh(returnStdout: true, script: "ls orion-content*.zip").trim()
            deployPackage(admin, orion_content, instance)
          } catch (Exception ex) {
            log.printRed("[ERROR] Package orion-content couldn't be found in current folder")
            log.printRed("[ERROR] ${ex}")
          }
        } else {
          log.printMagenta("[INFO] Skipping content deployment")
        }

        if (deploy_chromecast == true ) {
          try {
            orion_chromecast = sh(returnStdout: true, script: "ls orion-caf-receiver-chromecast*.zip").trim()
            deployPackage(admin, orion_chromecast, instance)
          } catch (Exception ex) {
            log.printRed("[ERROR] Package orion-caf-receiver-chromecast couldn't be found in current folder")
            log.printRed("[ERROR] ${ex}")
          }
        } else {
          log.printMagenta("[INFO] Skipping chromecast deployment")
        }
      }
    }
  }
}

def checkArtifact(configObject) {
}

def invalidateCache(configObject) {
  configObject.dispatchers.each {dispatcher ->
    log.printMagenta("[INFO] Invalidating cache on ${dispatcher}")
    sh(script: "-H 'CQ-Action: Delete' -H 'CQ-Handle: /' -H 'CQ-Path: /' -H 'Content-Length:0' -H 'Content-Type: application/octet-stream' --noproxy .com http://${dispatcher}/invalidate.cache")
  }
}

def flushJsp(configObject) {
  instances = collectAemInstances(configObject)
  withCredentials([usernameColonPassword(credentialsId: configObject.global.aem_admin_id, variable: 'admin')]){
    instances.each {instance ->
      log.printMagenta("[INFO] Sending cURL to slingjsp on ${instance}")
      sh(script: 'curl -u '+admin+' -X POST http://'+instance+'/system/console/fsclassloader -d "clear=true"')
      // sh(script: "curl -u ${admin} -X POST http://${instance}/system/console/scriptcache")
    }
  }
}

def packageArtifact(name) {
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
        log.printRed(ex)
      }
    }
  }
}

def packageConfig(name) {
  dir('artifacts'){
    def packages = ['orion-config']
    packages.each { pack ->
      try {
        log.printMagenta("[INFO] Copying ${pack} to artifacts folder")
        sh(script: "cp ../${pack}/target/*.zip ./")
      } catch (Exception ex) {
        log.printRed("[ERROR] Package ${pack} could not be found")
        log.printRed(ex)
      }
    }
  }
}

// def clearJspCache65(configObject) {
//   instances = collectAemInstances(configObject)
//   instances.each {instance ->
//     // def bundle_path = sh(script: "ssh -o StrictHostKeyChecking=no aem@${instance[0..-6]} 'grep -rn org.apache.sling.commons.fsclassloader /opt/aem/author/crx-quickstart/launchpad/felix/*'")
//     // def pattern = /^(.+).bundle.info/
//     // def result = bundle_path =~ pattern

//     // log.printMagenta("Found path ${result[0][1]}")
    
//     try {
//       log.printMagenta("Cleaning folder")
//       sh(script: "ssh -o StrictHostKeyChecking=no aem@${instance[0..-6]} sudo rm -rf /opt/aem/author/crx-quickstart/launchpad/felix/bundle526/data/classes")
//     } catch (Exception ex) {
//       log.printRed("[ERROR] Unable to remove folder")
//       log.printRed(ex)
//     }
//   }
// }
