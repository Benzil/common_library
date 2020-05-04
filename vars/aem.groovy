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
    sh(script: "curl -I -X','GET','--header CQ-Action: Delete','--header CQ-Handle:/content --header CQ-Path:/content http://${dispatcher}/invalidate.cache")
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

def buildArtifact(configObject) {
  configObject.global.components.getValue().each { component ->
    log.printGreen("[DEBUG] ${component.getClass()}")
    log.printGreen("[DEBUG] ${component.folder}")
    log.printGreen("[DEBUG] ${component.params}")
    log.printMagenta("[INFO] Compiling ${component.folder}")
    configFileProvider([configFile(fileId: 'maven_settings', variable: 'MAVEN_SETTINGS_XML')]){
      sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -DnewVersion=${configObject.global.version} -f ./${component.folder}/pom.xml clean versions:set versions:commit")
      sh(script: "mvn -s ${MAVEN_SETTINGS_XML} -f ./${component.folder}/pom.xml package ${component.params}")
    }
  }
}