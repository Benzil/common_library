// This method works only if 
def calculateStack(environment) {
  configFileProvider([configFile(fileId: 'opsworks_stacks', variable: 'STACKS')]){
    stacks = readJSON file: "${STACKS}"
    try {
      stack = stacks."${environment}"
      def instances = [authors: stack.authors, publishers: stack.publishers, dispatchers: stack.dispatchers]
      return instances
    } catch(Exception ex) {
      println "[ERROR] Can't find provided environment"
      println "[ERROR] Check if provided environment name exists in a config"
    }
  }
}

def invalidateCache(environment) {
  instances = calculateStack(environment)
  dispatchers = instances.dispatchers

  dispatchers.each {dispatcher ->
    def response = ['curl','-X','GET','--header', 'CQ-Action: Delete','--header', 'CQ-Handle:/content', '--header', 'CQ-Path:/content', "http://${dispatcher}/invalidate.cache"].execute().text
    println response
  }
}