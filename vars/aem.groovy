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
      // Uncomment to debug asigning
      // configObject.authors.each {author ->
      //   log.printMagenta("[INFO] ${author}")
      // }
      // configObject.publishers.each {publisher ->
      //   log.printMagenta("[INFO] ${publisher}")
      // }
      // configObject.dispatchers.each {dispatcher ->
      //   log.printMagenta("[INFO] ${dispatcher}")
      // }
    } catch(Exception ex) {
      log.printRed("[ERROR] Can't find provided environment")
      log.printRed("[ERROR] Check if ${environment} is in a config and it has authors, publishers, dispatchers defined")
      currentBuild.result = 'FAILURE'
    }

    return configObject
  }
}

def invalidateCache(configObject) {
  configObject.dispatchers.each {dispatcher ->
    def response = ['curl', '-I', '-X','GET','--header', 'CQ-Action: Delete','--header', 'CQ-Handle:/content', '--header', 'CQ-Path:/content', "http://${dispatcher}/invalidate.cache"].execute().text
    log.checkCurl(response)
  }
}

def collectAemInstances(configObject) {
  def instances = []
  instances.addAll(configObject.authors)
  instances.addAll(configObject.publishers)
  return instances
}

def flushJsp(configObject) {
  instances = collectAemInstances(configObject)
  withCredentials([usernameColonPassword(credentialsId: configObject.global.aem_admin_id, variable: 'admin')]){
    instances.each {instance ->
      log.printMagenta("[INFO] Sending cURL to slingjsp on ${instance}")
      // def sling_response = ["curl", "-I","-u", "${admin}", "-X", "POST", "http://${instance}/system/console/slingjsp"].execute().text
      sh(script: "curl -u ${admin} -X POST http://${instance}/system/console/slingjsp")
      // log.checkCurl(sling_response)

      log.printMagenta("[INFO] Sending cURL to scriptcache on ${instance}")
      // def script_response = ["curl", "-I","-u", "${admin}", "-X", "POST", "http://${instance}/system/console/scriptcache"].execute().text
      sh(script: "curl -u ${admin} -X POST http://${instance}/system/console/scriptcache")
      // log.checkCurl(script_response)
    }
  }
}

// Without checks cause respond seems to be too long
def refreshBundles(configObject) {
  instances = collectAemInstances(configObject)
  withCredentials([usernameColonPassword(credentialsId: configObject.global.aem_admin_id, variable: 'admin')]){
    instances.each {instance ->
      // log.printMagenta("[INFO] Sending cURL to refresh bundles on ${instance}")
      sh(script: "curl -u ${admin} -X POST -F action=refreshPackages http://${instance}/system/console/bundles")
      // def response = ["curl", "-u", "${admin}", "-X", "POST", "-F", "action=refreshPackages", "http://${instance}/system/console/bundles"].execute().text
    }
  }
}