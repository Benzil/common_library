def calculateStack(environment) {
  configFileProvider([configFile(fileId: 'opsworks_stacks', variable: 'STACKS')]){
    stacks = readJSON file: "${STACKS}"
    switch(environment) {
      case "lab2":
        stack = stacks.lab2;
        break;
      case "lab3b":
        stack = stacks.lab3b;
        break;
      case "lab5a":
        stack = stacks.lab5a;
        break;
      case "labe2esi":
        stack = stacks.labe2esi;
        break;
    }
  }
  println stack
  return stack
}