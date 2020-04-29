@Library('common_library') _

pipeline {
  agent any

  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
    withCredentials([usernameColonPassword(credentialsId: 'lab_aem_admin_user', variable: 'admin')])
  }

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
          aem.flushJsp(configObject, admin)
        }
      }
    }
  }
}