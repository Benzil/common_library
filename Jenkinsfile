@Library('common_library') _

pipeline {
  agent any
  stages {
    stage ('Testing') {
      steps {
        sh "echo TEST"
        script {
          println aem.calculateStack('lab2')
          println "=========================="
          println aem.calculateStack('lab3b')
          println "=========================="
          println aem.calculateStack('lab5a')
          println "=========================="
          println aem.calculateStack('labe2esi')
        }
      }
    }

    stage ('Testing lab2') {
      steps {
        script {
          instances = aem.calculateStack('lab2')

          println instances.authors
          println "=========================="
          println instances.publishers
          println "=========================="
          println instances.dispatchers
        }
      }
    }

    stage ('Testing labe2si') {
      steps {
        script {
          instances = aem.calculateStack('labe2esi')

          println instances.authors
          println "=========================="
          println instances.publishers
          println "=========================="
          println instances.dispatchers
        }
      }
    }

    stage ('Testing lab3b') {
      steps {
        script {
          instances = aem.calculateStack('labe3b')

          println instances.authors
          println "=========================="
          println instances.publishers
          println "=========================="
          println instances.dispatchers
        }
      }
    }

    stage ('Testing lab5a') {
      steps {
        script {
          instances = aem.calculateStack('lab5a')

          println instances.authors
          println "=========================="
          println instances.publishers
          println "=========================="
          println instances.dispatchers
        }
      }
    }
  }
}