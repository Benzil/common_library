@Library('common_library') _

pipeline {
  agent any
  stages {
    stage ('Testing') {
      steps {
        sh "echo TEST"
        script {
          test.calculateStack('lab2')
          test.calculateStack('lab3b')
          test.calculateStack('lab5a')
          test.calculateStack('labe2esi')
        }
      }
    }
  }
}