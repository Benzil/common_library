// Config provider plugin needed
// Config should be in JSON format 

def calculateConfig(environment) {
  configFileProvider([configFile(fileId: 'global_config', variable: 'CONFIG')]){
    config = readJSON file: config
    
  }
}