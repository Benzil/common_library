evaluate(new File("log.groovy"))

// This method works only if 
def calculateStack(environment) {
  configFileProvider([configFile(fileId: 'opsworks_stacks', variable: 'STACKS')]){
    stacks = readJSON file: "${STACKS}"
    stack = stacks."${environment}"

    def instances = [authors: stack.authors, publishers: stack.publishers, dispatchers: stack.dispatchers]
    return instances
  }
}

def invalidateCache(environment) {
  instances = calculateStack(environment)
  dispatchers = instances.dispatchers

  dispatchers.each {dispatcher ->
    def response = ['curl','-X','GET','--header', 'CQ-Action: Delete','--header', 'CQ-Handle:/content', '--header', 'CQ-Path:/content', "http://${dispatcher}/invalidate.cache"].execute().text
    paintGreen(response)
  }
}