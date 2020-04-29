@Library('common_library') _

pipeline {
  agent any
  stages {
    stage ('Init') {
      steps {
        script {
          aem.invalidateCache('lab2')
        }
      }
    }
  }
}