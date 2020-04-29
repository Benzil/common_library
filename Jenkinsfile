@Library('common_library') _

pipeline {
  agent any
  stages {
    stage ('Init') {
      steps {
        config.calculateConfig('lab2')
        config.calculateConfig('lab3b')
        config.calculateConfig('labe2esi')
        config.calculateConfig('lab5a')
      }
    }

    stage ('Error test') {
      steps {
        config.calculateConfig('lab2asdasd')
      }
    }
  }
}