// This method works only if 
def calculateStack(environment) {
  configFileProvider([configFile(fileId: 'opsworks_stacks', variable: 'STACKS')]){
    stacks = readJSON file: "${STACKS}"
    stack = stacks."${environment}"

    def instances = [authors: stack.authors, publishers: stack.publishers, dispatchers: stack.dispatchers]
    return instances
  }
}