pipeline {
   agent any

   options {
      buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
   }

   environment {
      NEXUS_JENKINS_CREDENTIALS = credentials('NEXUS_JENKINS_CREDENTIALS')
   }

   stages {
      stage('Say Hello') {

      }
   }
}
