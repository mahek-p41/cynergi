pipeline {
   agent any

   options {
      buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
      disableConcurrentBuilds()
      lock resource: 'cynergitestdeploydb'
   }

   environment {
      NEXUS_JENKINS_CREDENTIALS = credentials('NEXUS_JENKINS_CREDENTIALS')
      CYNERGI_DEPLOY_JENKINS = credentials('CYNERGI_DEPLOY_JENKINS_USER')
   }

   stages {
      stage('Start Postgres Database') {
         steps {
            dir('./support/deployment') {
               sh 'docker-compose rm -f'
               sh 'docker-compose build cynergitestdeploydb'
               sh 'docker-compose up -d cynergitestdeploydb'
               sh 'sleep 10'
            }
         }
      }

      stage('Build Deployment') {
         steps {
            dir('./support/deployment') {
               sh './build-cynergi-middleware.sh'
            }
         }
      }

      stage('Deploy DEVELOP Artifacts') {
         when {branch 'develop'}

         steps {
            script {
               gradleProps = readProperties file: 'gradle.properties'
               sh "curl -vf -u$NEXUS_JENKINS_CREDENTIALS_USR:$NEXUS_JENKINS_CREDENTIALS_PSW --upload-file ./build/libs/cynergi-middleware-${gradleProps.releaseVersion}-all.jar http://172.28.1.6/nexus/repository/CYNERGI-SNAPSHOT/cynergi-middleware.DEVELOP-${gradleProps.releaseVersion}.jar"
               sh "curl -vf -u$NEXUS_JENKINS_CREDENTIALS_USR:$NEXUS_JENKINS_CREDENTIALS_PSW --upload-file ./build/libs/cynergi-middleware.tar.xz http://172.28.1.6/nexus/repository/CYNERGI-SNAPSHOT/cynergi-middleware.DEVELOP-${gradleProps.releaseVersion}.tar.xz"
            }
         }
      }

      stage('Deploy STAGING Artifacts') {
         when {branch 'staging'}

         steps {
            script {
               gradleProps = readProperties file: 'gradle.properties'
               sh "curl -vf -u$NEXUS_JENKINS_CREDENTIALS_USR:$NEXUS_JENKINS_CREDENTIALS_PSW --upload-file ./build/libs/cynergi-middleware-${gradleProps.releaseVersion}-all.jar http://172.28.1.6/nexus/repository/CYNERGI-SNAPSHOT/cynergi-middleware.STAGING-${gradleProps.releaseVersion}.jar"
               sh "curl -vf -u$NEXUS_JENKINS_CREDENTIALS_USR:$NEXUS_JENKINS_CREDENTIALS_PSW --upload-file ./build/libs/cynergi-middleware.tar.xz http://172.28.1.6/nexus/repository/CYNERGI-SNAPSHOT/cynergi-middleware.STAGING-${gradleProps.releaseVersion}.tar.xz"
               sh "sshpass -p '$CYNERGI_DEPLOY_JENKINS_PSW' scp -v $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17 ./build/libs/cynergi-middleware.tar.xz /home/jenkins/JENKINS/STAGING/cynergi-middleware.tar.xz"
            }
         }
      }

      stage('Deploy RELEASE Artifacts') {
         when {branch 'master'}

         steps {
            script {
               gradleProps = readProperties file: 'gradle.properties'
               sh "curl -vf -u$NEXUS_JENKINS_CREDENTIALS_USR:$NEXUS_JENKINS_CREDENTIALS_PSW --upload-file ./build/libs/cynergi-middleware-${gradleProps.releaseVersion}-all.jar http://172.28.1.6/nexus/repository/CYNERGI-RELEASE/cynergi-middleware.RELEASE-${gradleProps.releaseVersion}.jar"
               sh "curl -vf -u$NEXUS_JENKINS_CREDENTIALS_USR:$NEXUS_JENKINS_CREDENTIALS_PSW --upload-file ./build/libs/cynergi-middleware.tar.xz http://172.28.1.6/nexus/repository/CYNERGI-RELEASE/cynergi-middleware.RELEASE-${gradleProps.releaseVersion}.tar.xz"
               sh "sshpass -p '$CYNERGI_DEPLOY_JENKINS_PSW' scp -v $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17  ./build/libs/cynergi-middleware.tar.xz /home/jenkins/JENKINS/RELEASE/cynergi-middleware.tar.xz"
            }
         }
      }
   }

   post {
      always {
         dir('./support/deployment') {
            sh 'docker-compose down && docker-compose rm -f'
         }
         dir('./build/reports/tests/test') {
            sh "rm -rf /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/test-results"
            sh "mkdir -p /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/test-results"
            sh "cp -rv * /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/test-results"
         }
         dir('./build/reports/openapi') {
            sh "rm -rf /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/api-docs"
            sh "mkdir -p /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/api-docs"
            sh "cp -rv * /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/api-docs"
         }
         dir('./build/reports/jacoco/test/html') {
            sh "rm -rf /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/code-coverage"
            sh "mkdir -p /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/code-coverage"
            sh "cp -rv * /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/code-coverage"
         }
         junit 'build/test-results/**/*.xml'
      }
   }
}
