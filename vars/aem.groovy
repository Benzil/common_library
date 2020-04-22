// This method works only if 
def calculateStack(environment) {
  configFileProvider([configFile(fileId: 'opsworks_stacks', variable: 'STACKS')]){
    stacks = readJSON file: "${STACKS}"
    // switch(environment) {
    //   case "lab2":
    //     stack = stacks.lab2;
    //     break;
    //   case "lab3b":
    //     stack = stacks.lab3b;
    //     break;
    //   case "lab5a":
    //     stack = stacks.lab5a;
    //     break;
    //   case "labe2esi":
    //     stack = stacks.labe2esi;
    //     break;
    //   case "prod-a":
    //     stack = stacks.proda;
    //     break;
    //   case "prod-b":
    //     stack = stacks.prodb;
    //     break;
    //   case "prod-c":
    //     stack = stacks.prodc;
    //     break;
    //   case "prod-d":
    //     stack = stacks.prodd;
    //     break;
    //   case "prod-e":
    //     stack = stacks.prode;
    //     break;
    //   case "prod-f":
    //     stack = stacks.prodf;
    //     break;
    //   case "prod-h":
    //     stack = stacks.prodh;
    //     break;
    //   case "prod-g":
    //     stack = stacks.prodg;
    //     break;
    //   case "preprod-eu":
    //     stack = stacks.preprodeu;
    //     break;
    // }
    stack = stacks.environment

    def instances = [authors: stack.authors, publishers: stack.publishers, dispatchers: stack.dispatchers]
    return instances
  }
}