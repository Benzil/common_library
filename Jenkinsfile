@Library('common_library') _

pipeline {
  agent any

  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
  }

  parameters {
    choice(name: 'environment', choices: ['lab2','lab3b','lab5a','labe2esi'], description: '')
  }

  stages {
    stage ('Init') {
      steps {
        script {
          configObject = aem.calculateConfig(params.environment)
          currentBuild.displayName = params.environment
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