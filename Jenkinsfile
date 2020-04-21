@Library('common_library') _

pipeline {
  agent any
  stages {
    stage ('Testing') {
      steps {
        sh "echo TEST"
        script {
          aem.calculateStack('lab2')
          aem.calculateStack('lab3b')
          aem.calculateStack('lab5a')
          aem.calculateStack('labe2esi')
        }
      }
    }
  }
}