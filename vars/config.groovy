// Config provider plugin needed
// Config should be in JSON format 

def calculateConfig(environment) {
  configFileProvider([configFile(fileId: 'web_config', variable: 'CONFIG')]){
    def conf_map = readJSON file: "${CONFIG}"
    try {
      stack = conf_map."${environment}"
      def instances = [authors: stack.authors, publishers: stack.publishers, dispatchers: stack.dispatchers]
      log.printMagenta("[INFO] Found stack for ${environment}")
      instances.authors.each {author ->
        log.printMagenta("[INFO] Authors:")
        log.printMagenta("[INFO] ${author}")
      }
      instances.publishers.each {publisher ->
        log.printMagenta("[INFO] Publishers:")
        log.printMagenta("[INFO] ${publisher}")
      }
      instances.dispatchers.each {dispatcher ->
        log.printMagenta("[INFO] Dispatchers:")
        log.printMagenta("[INFO] ${dispatcher}")
      }
      return instances.authors
      return insatnces.publishers
      return instances.dispatchers      
    } catch(Exception ex) {
      log.printRed("[ERROR] Can't find provided environment")
      log.printRed("[ERROR] Check if ${environment} is in a config")
      currentBuild.result = 'FAILURE'
    }
  }
}