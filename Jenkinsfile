@Library('common_library') _

pipeline {
  agent any
  stages {
    stage ('Init') {
      steps {
        aem.calculateConfig('lab2')
        aem.calculateConfig('lab3b')
        aem.calculateConfig('labe2esi')
        aem.calculateConfig('lab5a')
      }
    }

    stage ('Error test') {
      steps {
        aem.calculateConfig('lab23')
      }
    }
  }
}