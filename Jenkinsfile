pipeline {
   agent { label 'docs' }

   options {
      buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
      disableConcurrentBuilds()
   }

   environment {
      NEXUS_JENKINS_CREDENTIALS = credentials('NEXUS_JENKINS_CREDENTIALS')
      CYNERGI_DEPLOY_JENKINS = credentials('CYNERGI_DEPLOY_JENKINS_USER')
      def releaseVersion = readProperties(file: 'gradle.properties')['releaseVersion'].trim()
      def networkId = UUID.randomUUID().toString()
      def jenkinsUid = sh(script: 'id -u', returnStdout: true).trim()
      def jenkinsGid = sh(script: 'id -g', returnStdout: true).trim()
      def micronautEnv = 'prod'
   }

   stages {
      stage('Setup Docker') {
         steps {
            script {
                if (env.BRANCH_NAME.length() > 50) {
                    currentBuild.result = 'ABORTED'
                    error("Branch name is too long: ${env.BRANCH_NAME} (${env.BRANCH_NAME.length()} characters). Maximum length is 50 characters.")
                } else {
                    sh 'docker network create ${networkId}'
                    sh 'mkdir -p gradleCache gradleWrapper'
                }
            }
         }
      }

      stage('Test') {
         when { not { anyOf { branch 'master'; branch 'staging'; branch 'develop'; } } }

         steps {
            script {
               def cynergibasedb = docker.build("cynergibasedb:${env.BRANCH_NAME}", "-f ./support/development/cynergibasedb/cynergibasedb.dockerfile ./support/development/cynergibasedb")
               def cynergitestdb = docker.build("cynergitestdb:${env.BRANCH_NAME}", "-f ./support/development/cynergitestdb/cynergitestdb.dockerfile --build-arg LOG_STATEMENT=all --build-arg DB_IMAGE=cynergibasedb:${env.BRANCH_NAME} ./support/development/cynergitestdb")
               def sftpTestServer = docker.build("cynergitestsftp:${env.BRANCH_NAME}", "-f ./support/development/sftp/sftp.dockerfile --build-arg USER_ID=$jenkinsUid --build-arg GROUP_ID=$jenkinsGid ./support/development/sftp")
               def cynmid = docker.build("middleware:${env.BRANCH_NAME}", "-f ./support/deployment/cynmid/cynmid.dockerfile --build-arg USER_ID=$jenkinsUid --build-arg GROUP_ID=$jenkinsGid ./support/deployment/cynmid")

               sh 'mkdir -p /tmp/sftpuser'

               cynergitestdb.withRun("--network ${networkId} --name cynergitestdb${env.BRANCH_NAME} -e POSTGRES_PASSWORD=password --tmpfs /var/lib/postgresql/data:rw -v ${workspace}/support/development/cynergitestdb/fastinfo:/tmp/fastinfo") { cdbt ->
                  sftpTestServer.withRun("--network ${networkId} --name cynergitestsftp${env.BRANCH_NAME}") { sftp ->
                      script {
                         sh "docker run -i --rm --network ${networkId} cynergibasedb:${env.BRANCH_NAME} /opt/scripts/db-ready.sh cynergitestdb${env.BRANCH_NAME}"

                         cynmid.inside(
                            "--network ${networkId} "+
                            "-v ${workspace}/gradleCache:/home/jenkins/caches " +
                            "-v ${workspace}/gradleWrapper:/home/jenkins/wrapper " +
                            "-e DATASOURCES_DEFAULT_URL=jdbc:postgresql://cynergitestdb${env.BRANCH_NAME}:5432/postgres " +
                            "-e TEST_SFTP_HOSTNAME=cynergitestsftp${env.BRANCH_NAME} " +
                            "-e TEST_SFTP_PORT=22 "
                         ) {
                            sh '''#!/usr/bin/env bash
                            set -x
                            set -o errexit -o pipefail -o noclobber -o nounset
                            export JAVA_OPTS="-Xms2048m -Xmx2048m -Xgcpolicy:gencon"

                            ./gradlew --no-daemon --stacktrace clean buildApiDocs 2>&1 1>/dev/null
                            ./gradlew --no-daemon test jacocoTestReport
                            '''
                         }
                      }
                  }
               }
            }
         }
      }

      stage('Build tarball') {
         when { anyOf { branch 'master'; branch 'staging'; branch 'develop'; } }

         steps {
            script {
               def cynmidtar = docker.build("middlewaretar:${env.BRANCH_NAME}", "-f ./support/deployment/cynmid/cynmid.dockerfile --build-arg USER_ID=$jenkinsUid --build-arg GROUP_ID=$jenkinsGid --build-arg GROOVY_VER=4.0.3 ./support/deployment/cynmid")

               cynmidtar.inside(
                  "-v ${workspace}/gradleCache:/home/jenkins/caches " +
                  "-v ${workspace}/gradleWrapper:/home/jenkins/wrapper " +
                  "-v ${workspace}:/home/jenkins/cynergi-middleware " +
                  "-e BUILD_VERSION=${releaseVersion} " +
                  "-e MICRONAUT_ENV=${micronautEnv}"
               ) {
                  sh '''#!/usr/bin/env bash
                  set -o errexit -o pipefail -o nounset -o noclobber
                  export JAVA_OPTS="-Xms1024m -Xmx1024m -Xgcpolicy:gencon"
                  VER_BUILD=$(java -version 2>&1 | awk '/build/ {gsub("\\)","") ; print $NF}' | head -n 1)

                  ./gradlew --no-daemon --stacktrace shadowJar

                  mkdir -p /opt/cyn/v01/cynmid/data/
                  cp /home/jenkins/cynergi-middleware/support/deployment/cyndsets-parse.sh /opt/cyn/v01/cynmid/data/cyndsets-parse.sh
                  chmod u+x /opt/cyn/v01/cynmid/data/cyndsets-parse.sh

                  mkdir -p /opt/cyn/v01/cynmid/scripts/
                  mkdir -p /opt/cyn/v01/cynmid/groovy/bin/
                  cp /home/jenkins/cynergi-middleware/support/deployment/cynergi-postgres-check.sh /opt/cyn/v01/cynmid/scripts/cynergi-postgres-check.sh
                  cp /home/jenkins/cynergi-middleware/support/development/cynergidb/*.groovy /opt/cyn/v01/cynmid/scripts/
                  cp /home/jenkins/cynergi-middleware/support/development/cynergibasedb/*.groovy /opt/cyn/v01/cynmid/scripts/
                  chmod u+x /opt/cyn/v01/cynmid/scripts/*.groovy
                  chmod u+x /opt/cyn/v01/cynmid/scripts/cynergi-postgres-check.sh
                  jlink --module-path "$JAVA_HOME\\jmods" \\
                     --compress 2 \\
                     --no-header-files \\
                     --no-man-pages \\
                     --add-modules java.base,java.compiler,java.datatransfer,java.desktop,java.instrument,java.logging,java.management,java.management.rmi,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.se,java.security.jgss,java.security.sasl,java.smartcardio,java.sql,java.sql.rowset,java.transaction.xa,java.xml,java.xml.crypto,jdk.accessibility,jdk.attach,jdk.charsets,jdk.compiler,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.dynalink,jdk.editpad,jdk.internal.ed,jdk.internal.jvmstat,jdk.internal.le,jdk.internal.opt,jdk.jcmd,jdk.jdeps,jdk.jdi,jdk.jdwp.agent,jdk.jsobject,jdk.localedata,jdk.management,jdk.management.agent,jdk.naming.dns,jdk.naming.ldap,jdk.naming.rmi,jdk.net,jdk.pack,jdk.rmic,jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported,jdk.unsupported.desktop,jdk.xml.dom,jdk.zipfs,openj9.dataaccess,openj9.dtfj,openj9.dtfjview,openj9.gpu,openj9.jvm,openj9.sharedclasses,openj9.traceformat \\
                     --strip-debug \\
                     --output /opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}
                  mkdir -p /opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}/jitcache

                  cp /home/jenkins/cynergi-middleware/support/deployment/cynergi-middleware.httpd.conf /opt/cyn/v01/cynmid/cynergi-middleware.httpd.conf
                  ln -s /opt/cyn/v01/cynmid/java/openj9/${VER_BUILD} /opt/cyn/v01/cynmid/java/current
                  ln -s /opt/cyn/v01/cynmid/groovy/${GROOVY_VERSION} /opt/cyn/v01/cynmid/groovy/current
                  sed "s/@@MICRONAUT_ENV@@/${MICRONAUT_ENV}/g" /home/jenkins/cynergi-middleware/support/deployment/cynergi-middleware.conf > /opt/cyn/v01/cynmid/cynergi-middleware.conf
                  sed -i '/^#!\\/usr\\/bin\\/env.*/a export JAVA_HOME=/opt/cyn/v01/cynmid/java/current' /opt/cyn/v01/cynmid/groovy/current/bin/startGroovy
                  sed -i  '/^export\\ JAVA_HOME=\\/opt\\/cyn\\/v01\\/cynmid\\/java\\/current/a export GROOVY_HOME=/opt/cyn/v01/cynmid/groovy/current' /opt/cyn/v01/cynmid/groovy/current/bin/startGroovy
                  cp /home/jenkins/cynergi-middleware/support/development/cynergidb/setup-database.sql /opt/cyn/v01/cynmid/data/
                  cp /home/jenkins/cynergi-middleware/build/libs/cynergi-middleware.jar /opt/cyn/v01/cynmid/cynergi-middleware.jar
                  mkdir -p /opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}/jitcache
                  rm -f /home/jenkins/cynergi-middleware/build/libs/cynergi-middleware.tar.xz
                  tar -c --owner=0 --group=0 --to-stdout /opt/cyn | xz -6 - > /home/jenkins/cynergi-middleware/build/libs/cynergi-middleware.tar.xz
                  '''
               }
            }
         }
      }

      stage('Upload DEVELOP Artifacts to Nexus') {
         when { branch 'develop' }

         steps {
            script {
               sh '''#!/usr/bin/env bash
               set -o errexit -o pipefail -o nounset -o noclobber

               curl -vf -u$NEXUS_JENKINS_CREDENTIALS_USR:$NEXUS_JENKINS_CREDENTIALS_PSW --upload-file ./build/libs/$(ls -lrt build/libs | grep all\\.jar | awk '{print $9}' | head -n 1) http://172.28.1.6/nexus/repository/CYNERGI-SNAPSHOT/cynergi-middleware.DEVELOP-${releaseVersion}.jar
               curl -vf -u$NEXUS_JENKINS_CREDENTIALS_USR:$NEXUS_JENKINS_CREDENTIALS_PSW --upload-file ./build/libs/$(ls -lrt build/libs | grep tar.xz | awk '{print $9}' | head -n 1) http://172.28.1.6/nexus/repository/CYNERGI-SNAPSHOT/cynergi-middleware.DEVELOP-${releaseVersion}.tar.xz
               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW scp -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa ./build/libs/$(ls -lrt build/libs | grep tar.xz | awk '{print $9}' | head -n 1) $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17:/home/jenkins/ELIMINATION/DEVELOP/cynergi-middleware-${releaseVersion}.tar.xz
               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW ssh -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17 bash -c "'ln -f /home/jenkins/ELIMINATION/DEVELOP/cynergi-middleware-${releaseVersion}.tar.xz /home/jenkins/ELIMINATION/DEVELOP/cynergi-middleware-current.tar.xz'"
               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW ssh -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17 bash -c "'touch /home/jenkins/ELIMINATION/DEVELOP/build.trigger'"
               '''
            }
         }
      }

      stage('Deploy DEVELOP Artifacts') {
         when { branch 'develop' }

         steps {
            script {
               sh '''#!/usr/bin/env bash
               set -o errexit -o pipefail -o nounset -o noclobber

               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW scp -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa ./build/libs/$(ls -lrt build/libs | grep tar.xz | awk '{print $9}' | head -n 1) $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17:/home/jenkins/ELIMINATION/DEVELOP/cynergi-middleware-${releaseVersion}.tar.xz
               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW ssh -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17 bash -c "'ln -f /home/jenkins/ELIMINATION/DEVELOP/cynergi-middleware-${releaseVersion}.tar.xz /home/jenkins/ELIMINATION/DEVELOP/cynergi-middleware-current.tar.xz'"
               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW ssh -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17 bash -c "'touch /home/jenkins/ELIMINATION/DEVELOP/build.trigger'"
               '''
            }
         }
      }

      stage('Upload STAGING Artifacts to Nexus') {
         when { anyOf { branch 'staging'; } }

         steps {
            script {
               sh '''#!/usr/bin/env bash
               set -o errexit -o pipefail -o nounset -o noclobber

               curl -vf -u$NEXUS_JENKINS_CREDENTIALS_USR:$NEXUS_JENKINS_CREDENTIALS_PSW --upload-file ./build/libs/$(ls -lrt build/libs | grep all\\.jar | awk '{print $9}' | head -n 1) http://172.28.1.6/nexus/repository/CYNERGI-SNAPSHOT/cynergi-middleware.STAGING-${releaseVersion}.jar
               curl -vf -u$NEXUS_JENKINS_CREDENTIALS_USR:$NEXUS_JENKINS_CREDENTIALS_PSW --upload-file ./build/libs/$(ls -lrt build/libs | grep tar.xz | awk '{print $9}' | head -n 1) http://172.28.1.6/nexus/repository/CYNERGI-SNAPSHOT/cynergi-middleware.STAGING-${releaseVersion}.tar.xz
               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW scp -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa ./build/libs/$(ls -lrt build/libs | grep tar.xz | awk '{print $9}' | head -n 1) $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17:/home/jenkins/ELIMINATION/STAGING/cynergi-middleware-${releaseVersion}.tar.xz
               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW ssh -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17 bash -c "'ln -f /home/jenkins/ELIMINATION/STAGING/cynergi-middleware-${releaseVersion}.tar.xz /home/jenkins/ELIMINATION/STAGING/cynergi-middleware-current.tar.xz'"
               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW ssh -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17 bash -c "'touch /home/jenkins/ELIMINATION/STAGING/build.trigger'"
               '''
            }
         }
      }

      stage('Deploy STAGING Artifacts') {
         when { anyOf { branch 'staging'; } }

         steps {
            script {
               sh '''#!/usr/bin/env bash
               set -o errexit -o pipefail -o nounset -o noclobber

               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW scp -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa ./build/libs/$(ls -lrt build/libs | grep tar.xz | awk '{print $9}' | head -n 1) $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17:/home/jenkins/ELIMINATION/STAGING/cynergi-middleware-${releaseVersion}.tar.xz
               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW ssh -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17 bash -c "'ln -f /home/jenkins/ELIMINATION/STAGING/cynergi-middleware-${releaseVersion}.tar.xz /home/jenkins/ELIMINATION/STAGING/cynergi-middleware-current.tar.xz'"
               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW ssh -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17 bash -c "'touch /home/jenkins/ELIMINATION/STAGING/build.trigger'"
               '''
            }
         }
      }

      stage('Upload RELEASE Artifacts to Nexus') {
         when {branch 'master' }

         steps {
            script {
               sh '''#!/usr/bin/env bash
               set -o errexit -o pipefail -o nounset -o noclobber

               curl -vf -u$NEXUS_JENKINS_CREDENTIALS_USR:$NEXUS_JENKINS_CREDENTIALS_PSW --upload-file ./build/libs/$(ls -lrt build/libs | grep all\\.jar | awk '{print $9}' | head -n 1) http://172.28.1.6/nexus/repository/CYNERGI-RELEASE/cynergi-middleware.RELEASE-${releaseVersion}.jar
               curl -vf -u$NEXUS_JENKINS_CREDENTIALS_USR:$NEXUS_JENKINS_CREDENTIALS_PSW --upload-file ./build/libs/$(ls -lrt build/libs | grep tar.xz | awk '{print $9}' | head -n 1) http://172.28.1.6/nexus/repository/CYNERGI-RELEASE/cynergi-middleware.RELEASE-${releaseVersion}.tar.xz
               '''
            }
         }
      }

      stage('Deploy RELEASE Artifacts') {
         when {branch 'master' }

         steps {
            script {
               sh '''#!/usr/bin/env bash
               set -o errexit -o pipefail -o nounset -o noclobber

               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW scp -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa ./build/libs/$(ls -lrt build/libs | grep tar.xz | awk '{print $9}' | head -n 1) $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17:/home/jenkins/ELIMINATION/RELEASE/cynergi-middleware-${releaseVersion}.tar.xz
               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW ssh -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17 bash -c "'ln -f /home/jenkins/ELIMINATION/RELEASE/cynergi-middleware-${releaseVersion}.tar.xz /home/jenkins/ELIMINATION/RELEASE/cynergi-middleware-current.tar.xz'"
               sshpass -p $CYNERGI_DEPLOY_JENKINS_PSW ssh -oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa $CYNERGI_DEPLOY_JENKINS_USR@172.19.10.17 bash -c "'touch /home/jenkins/ELIMINATION/RELEASE/build.trigger'"
               '''
            }
         }
      }
   }

   post {
      always {
         script {
            sh "docker network rm ${networkId}"
            //Emails passed with function call will receive notifications on all merges,
            //or manualy builds of develop/master/staging.
            emailNotifications('')
            if (env.BRANCH_NAME == "develop" && currentBuild.currentResult == 'SUCCESS' ){
               echo "Starting Cypress E2E test suite."
               build wait: false, job:'../cynergi-e2e/master'
            }
         }
/*          dir('./build/reports/tests/test') {
            sh "rm -rf /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/test-results"
            sh "mkdir -p /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/test-results"
            sh "cp -r * /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/test-results"
         }
         dir('./build/reports/jacoco/test/html') {
            sh "rm -rf /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/code-coverage"
            sh "mkdir -p /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/code-coverage"
            sh "cp -r * /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/code-coverage"
         }
         junit 'build/test-results *//** /* *//*.xml'
dir('./build/reports/openapi') {
            sh "rm -rf /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/api-docs"
            sh "mkdir -p /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/api-docs"
            sh "cp -r * /usr/share/nginx/html/reports/cynergi-middleware/${env.BRANCH_NAME}/api-docs"
         } */
      }
   }
}

def emailNotifications(names){
   def toMailRecipients = "${names}"
   def jobName = currentBuild.fullDisplayName

   if (env.BRANCH_NAME == "develop" || env.BRANCH_NAME == "master" || env.BRANCH_NAME == "staging"){
      echo "Sending email to ${toMailRecipients}, requester and culprits."
      emailext body: '''${SCRIPT, template="groovy-html.template"}''',
      subject: "[Jenkins] ${jobName}",
      to: "${toMailRecipients}",
      recipientProviders: [[$class: 'CulpritsRecipientProvider'],[$class: 'RequesterRecipientProvider']]
   } else {
      //Getting email address from top commit on branch.
      //Workaround for empty changelist of first build of new branch.
      def useremail = sh(script: 'git log -1 --format="%ae"', returnStdout: true).trim()
      echo "Sending email to ${useremail}, and requester."
      emailext body: '''${SCRIPT, template="groovy-html.template"}''',
      subject: "[Jenkins] ${jobName}",
      to: "${useremail}",
      recipientProviders: [[$class: 'DevelopersRecipientProvider'],[$class: 'RequesterRecipientProvider']]
   }
}
