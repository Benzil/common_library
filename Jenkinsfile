@Library('common_library') _

pipeline {
  agent any
  stages {
    stage ('Testing lab2') {
      steps {
        script {
          instances = aem.calculateStack('lab2')

          log.printRed(instances.authors)
          log.printMagenta("==========================")
          log.printGreen(instances.publishers)
        }
      }
    }
  }
}