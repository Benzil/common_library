@Library('common_library') _

pipeline {
  agent any
  stages {
    stage ('Testing lab2') {
      steps {
        script {
          log.paintGreen(aem.invalidateCache('lab2'))
        }
      }
    }
  }
}