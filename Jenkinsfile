@Library('common_library') _

pipeline {
  agent any
  stages {
    stage ('Init') {
      steps {
        script {
          configObject = aem.calculateConfig('lab2')
        }
      }
    }

    stage ('Invalidate cache') {
      steps {
        script {
          aem.invalidateCache(configObject)
        }
      }
    }
  }
}