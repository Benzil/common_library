@Library('common_library') _

pipeline {
  agent any

  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
  }

  parameters {
    string(name: 'tag', defaultValue: '', description: '')
    choice(name: 'environment', choices: ['lab2','lab3b','lab5a','labe2esi'], description: '')
  }

  stages {
    stage ('Init') {
      steps {
        script {
          configObject = aem.calculateConfig(params.environment)
        }
      }
    }

    stage ('Checkout') {
      steps {
        checkout scm: [
          $class: 'GitSCM',
          branches: [[name: "refs/tags/${params.tag}"]],
          doGenerateSubmoduleConfigurations: false,
          extensions: [],
          submoduleCfg: [], 
          userRemoteConfigs: [[
            credentialsId: configObject.global.git_user_id,
            url: configObject.global.repository
          ]]
        ]
        
        script {
          package_json = readJSON file: "orion-frontend/package.json"
          configObject.global.version = package_json.version
          currentBuild.displayName = params.environment + "-" + configObject.global.version
        }
      }
    }

    stage ('Build') {
      steps {
        script {
          aem.buildArtifact(configObject)
        }
      }
    }

    stage ('Archive') {
      steps {
        script {
          aem.archiveArtifact(configObject, params.tag)
        }
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'artifacts/*.tar.gz'
    }
  }
}