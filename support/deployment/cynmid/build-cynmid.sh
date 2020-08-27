#!/usr/bin/env bash

set -o errexit -o pipefail -o noclobber -o nounset
export JAVA_OPTS="-Xms1024m -Xmx1024m -Xgcpolicy:gencon"
VER_BUILD=$(java -version 2>&1 | awk '/build/ {gsub("\)","") ; print $NF}' | head -n 1)

mkdir -p /opt/cyn/v01/cynmid/logs/
mkdir -p /opt/cyn/v01/cynmid/java/openj9/"${VER_BUILD}"/jitcache
export VERTX_PG_CLIENT_PORT=5432
export VERTX_PG_CLIENT_HOST=cynergitestdeploydb
cd /home/jenkins/cynergi-middleware
./gradlew --no-daemon --stacktrace clean test jacocoTestReport buildApiDocs shadowJar

BRANCH=$(git show-ref | grep `git rev-parse --verify HEAD` | awk '{print $NF}')
BRANCH=$(basename "$BRANCH")

if [ "$BRANCH" = "develop" ]; then
  export MICRONAUT_ENV="cstdevelop"
else
  export MICRONAUT_ENV="prod"
fi

mkdir -p /opt/cyn/v01/cynmid/data/
cp /home/jenkins/cynergi-middleware/support/deployment/cyndsets-parse.sh /opt/cyn/v01/cynmid/data/cyndsets-parse.sh
chmod u+x /opt/cyn/v01/cynmid/data/cyndsets-parse.sh

mkdir -p /opt/cyn/v01/cynmid/scripts/
cp /home/jenkins/cynergi-middleware/support/deployment/cynergi-postgres-check.sh /opt/cyn/v01/cynmid/scripts/cynergi-postgres-check.sh
chmod u+x /opt/cyn/v01/cynmid/scripts/cynergi-postgres-check.sh

cp /home/jenkins/cynergi-middleware/support/deployment/cynergi-middleware.httpd.conf /opt/cyn/v01/cynmid/cynergi-middleware.httpd.conf
sed "s/@@JAVA_VER_BUILD@@/${VER_BUILD}/g; s/@@MICRONAUT_ENV@@/${MICRONAUT_ENV}/g" /home/jenkins/cynergi-middleware/support/deployment/cynergi-middleware.conf > /opt/cyn/v01/cynmid/cynergi-middleware.conf
cp /home/jenkins/cynergi-middleware/support/development/cynergidb/setup-database.sql /opt/cyn/v01/cynmid/data/
cp /home/jenkins/cynergi-middleware/build/libs/cynergi-middleware*-all.jar /opt/cyn/v01/cynmid/cynergi-middleware.jar
mkdir -p /opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}/jitcache
cp -r /opt/java/openjdk/jre/. /opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}
tar -c --owner=0 --group=0 --to-stdout /opt/cyn | xz -6 - > /home/jenkins/cynergi-middleware/build/libs/cynergi-middleware.tar.xz
