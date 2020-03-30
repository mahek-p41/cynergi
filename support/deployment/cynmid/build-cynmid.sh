#!/usr/bin/env bash

set -o errexit -o pipefail -o noclobber -o nounset

VER_BUILD=$(java -version 2>&1 | awk '/build/ {gsub("\)","") ; print $NF}' | head -n 1)

mkdir -p /opt/cyn/v01/cynmid/logs/
mkdir -p /opt/cyn/v01/cynmid/java/openj9/"${VER_BUILD}"/jitcache
export POSTGRES_REACTIVE_CLIENT_PORT=5432
export POSTGRES_REACTIVE_CLIENT_HOST=cynergitestdeploydb
cd /home/jenkins/cynergi-middleware
./gradlew --no-daemon --stacktrace clean test jacocoTestReport buildApiDocs shadowJar

if [ $(git rev-parse --abbrev-ref HEAD) = "develop" ]; then
  export MICRONAUT_ENV="cstdevelop"
else
  export MICRONAUT_ENV="prod"
fi

mkdir -p /opt/cyn/v01/cynmid/data/
cp /home/jenkins/cynergi-middleware/support/deployment/cyndsets-parse.sh /opt/cyn/v01/cynmid/data/cyndsets-parse.sh
chmod u+x /opt/cyn/v01/cynmid/data/cyndsets-parse.sh
cp /home/jenkins/cynergi-middleware/support/deployment/cynergi-middleware.httpd.conf /opt/cyn/v01/cynmid/cynergi-middleware.httpd.conf
sed "s/@@JAVA_VER_BUILD@@/${VER_BUILD}/g; s/@@MICRONAUT_ENV@@/${MICRONAUT_ENV}/g" /home/jenkins/cynergi-middleware/support/deployment/cynergi-middleware.conf > /opt/cyn/v01/cynmid/cynergi-middleware.conf
cp /home/jenkins/cynergi-middleware/support/deployment/setup-database.sql /opt/cyn/v01/cynmid/data/
cp /home/jenkins/cynergi-middleware/build/libs/cynergi-middleware*-all.jar /opt/cyn/v01/cynmid/cynergi-middleware.jar
mkdir -p /opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}/jitcache
cp -r /opt/java/openjdk/jre/. /opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}
tar -c --owner=0 --group=0 --to-stdout /opt/cyn | xz -6 - > /home/jenkins/cynergi-middleware/build/libs/cynergi-middleware.tar.xz
