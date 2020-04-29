// Config provider plugin needed
// Config should be in JSON format 

def calculateConfig(environment) {
  configFileProvider([configFile(fileId: 'web_config', variable: 'CONFIG')]){
    def conf = readJSON file: "${CONFIG}"
    
    def configObject = [:]
    configObject["global"] = conf.global
    try {
      configObject["authors"] = conf["stacks"]["${environment}"]["authors"]
      configObject["publishers"] = conf["stacks"]["${environment}"]["publishers"]
      configObject["dispatchers"] = conf["stacks"]["${environment}"]["dispatchers"]
      log.printMagenta("[INFO] Found stack for ${environment}")
      configObject.authors.each {author ->
        log.printMagenta("[INFO] ${author}")
      }
      configObject.publishers.each {publisher ->
        log.printMagenta("[INFO] ${publisher}")
      }
      configObject.dispatchers.each {dispatcher ->
        log.printMagenta("[INFO] ${dispatcher}")
      }
    } catch(Exception ex) {
      log.printRed("[ERROR] Can't find provided environment")
      log.printRed("[ERROR] Check if ${environment} is in a config")
      currentBuild.result = 'FAILURE'
    }

    return configObject
  }
}