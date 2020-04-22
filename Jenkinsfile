@Library('common_library') _

pipeline {
  agent any
  stages {
    stage ('Testing') {
      steps {
        sh "echo TEST"
        script {
          println aem.calculateStack('lab2')
          println aem.calculateStack('lab3b')
          println aem.calculateStack('lab5a')
          println aem.calculateStack('labe2esi')
        }
      }
    }

    stage ('More testing') {
      steps {
        script {
          instances = aem.calculateStack('labe2esi')

          println instances.authors
          println instances.publishers
          println instances.dispatchers
        }
      }
    }
  }
}