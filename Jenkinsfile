@Library('common_library') _

pipeline {
  agent any

  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
  }

  stages {
    stage ('Init') {
      steps {
        script {
          configObject = aem.calculateConfig('lab2')
          currentBuild.displayName = 'lab2'
        }
      }
    }

    stage ('Flush JSP') {
      steps {
        script {
          aem.flushJsp(configObject)
        }
      }
    }

    stage ('Refresh bundles') {
      steps {
        script {
          aem.refreshBundles(configObject)
        }
      }
    }

  }
}