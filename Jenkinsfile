pipeline {
   agent any

   options {
      buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
   }

   environment {
      NEXUS_JENKINS_CREDENTIALS = credentials('NEXUS_JENKINS_CREDENTIALS')
   }

   stages {
      stage('Start Postgres Database') {
         steps {
            sh 'docker-compose rm -f && docker-compose build cynergitestdb && docker-compose up cynergitestdb'
         }
      }

      stage('Build Jar') {
         steps {
            sh './gradlew clean build --no-daemon'
         }
      }

      stage('Deploy SNAPSHOT Artifacts') {
         when { branch 'develop' }

         steps {
            script {
               gradleProps = readProperties file: 'gradle.properties'
               sh "curl -vf -u$NEXUS_JENKINS_CREDENTIALS_USR:$NEXUS_JENKINS_CREDENTIALS_PSW --upload-file ./build/libs/WebRACServices.jar http://172.28.1.6/nexus/repository/CYNERGI-SNAPSHOT/WebRACServices.SNAPSHOT-${gradleProps.releaseVersion}.jar"
            }
         }
      }

      stage('Deploy RELEASE Artifacts') {
         when { branch 'master' }

         steps {
            script {
               gradleProps = readProperties file: 'gradle.properties'
               sh "curl -vf -u$NEXUS_JENKINS_CREDENTIALS_USR:$NEXUS_JENKINS_CREDENTIALS_PSW --upload-file ./build/libs/WebRACServices.jar http://172.28.1.6/nexus/repository/CYNERGI-RELEASE/WebRACServices.RELEASE-${gradleProps.releaseVersion}.jar"
            }
         }
      }
   }

   post {
      always {
         sh 'docker-compose down && docker-compose rm -f'
      }
   }
}
