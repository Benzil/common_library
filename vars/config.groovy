// Config provider plugin needed
// Config should be in JSON format 

def calculateConfig(environment) {
  configFileProvider([configFile(fileId: 'web_config', variable: 'CONFIG')]){
    config = readJSON file: "${CONFIG}"
    try {
      stack = stacks."${environment}"
      def instances = [authors: stack.authors, publishers: stack.publishers, dispatchers: stack.dispatchers]
      log.printMagenta("[INFO] Found stack for ${environment}")
      instances.authors.each {author ->
        log.printMagenta("[INFO] ${author}")
      }
      instances.publishers.each {publisher ->
        log.printMagenta("[INFO] ${publisher}")
      }
      instances.dispatchers.each {dispatcher ->
        log.printMagenta("[INFO] ${dispatcher}")
      }
      return instances
      
    } catch(Exception ex) {
      log.printRed("[ERROR] Can't find provided environment")
      log.printRed("[ERROR] Check if ${environment} is in a config")
      currentBuild.result = 'FAILURE'
    }
  }
}