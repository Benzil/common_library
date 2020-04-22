@Library('common_library') _

pipeline {
  agent any
  stages {
    stage ('Testing lab2') {
      steps {
        script {
          aem.calculateStack('lab2')
          aem.invalidateCache('lab2')
          aem.calculateStack('lab212312')
        }
      }
    }
  }
}