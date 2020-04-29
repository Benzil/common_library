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
    if(response.contains('200')) {
      log.printMagenta("[INFO] Cache invalidated successfully")
    } else {
      log.printRed("[ERROR] Unable to invalidate cache on ${dispatcher}")
      currentBuild.result = 'UNSTABLE'
    }
  }
}

def curlSlingjsp(creds, instance, page) {
  def response = ["curl", "-I","-u", "${creds}", "-X", "POST", "http://${instance}/system/console/${page}"].execute().text
  if(response.contains('302')) {
      log.printMagenta("[INFO] POST to http://${instance}/system/console/${page} sent successfully")
    } else {
      log.printRed("[ERROR] Unable to send POST to http://${instance}/system/console/${page}")
      currentBuild.result = 'UNSTABLE'
    }
}

def flushJsp(configObject, creds) {
  def instances = []
  instances.addAll(configObject.authors)
  instances.addAll(configObject.publishers)

  instances.each {instance ->
    curlSlingjsp(creds, instance, 'slingjsp')
    curlSlingjsp(creds, instance, 'scriptcache')
  }
}