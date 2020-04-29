// This method works only if 
def calculateStack(environment) {
  configFileProvider([configFile(fileId: 'opsworks_stacks', variable: 'STACKS')]){
    stacks = readJSON file: "${STACKS}"
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

def calculateConfig() {
  
}

def invalidateCache(environment) {
  instances = calculateStack(environment)
  dispatchers = instances.dispatchers

  dispatchers.each {dispatcher ->
    def response = ['curl','-X','GET','--header', 'CQ-Action: Delete','--header', 'CQ-Handle:/content', '--header', 'CQ-Path:/content', "http://${dispatcher}/invalidate.cache"].execute().text
    if(response.contains('OK')) {
      log.printMagenta("[INFO] Cache invalidated successfully")
    } else {
      log.printRed("[ERROR] Unable to invalidate cache on ${dispatcher}")
      currentBuild.result = 'UNSTABLE'
    }
  }
}

def buildArtifact(tag) {

}

def saveArtifact() {

}

def deployArtifact(tag) {

}