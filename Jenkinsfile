@Library('common_library') _

pipeline {
  agent any
  stages {
    stage ('Testing') {
      steps {
        sh "echo TEST"
        script {
          test.testLibrary()
        }
      }
    }
  }
}