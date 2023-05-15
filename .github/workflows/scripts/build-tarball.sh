#!/usr/bin/env bash
set -o errexit -o pipefail -o nounset -o noclobber
export JAVA_OPTS="-Xms1024m -Xmx1024m -Xgcpolicy:gencon"
VER_BUILD=$(java -version 2>&1 | awk '/build/ {gsub("\)","") ; print $NF}' | head -n 1)
buildPath="build/libs"
deployFile="cynergi-middleware-${RELEASE_VERSION}.tar.xz"
jarFile="cynergi-middleware.jar"

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
jlink --module-path "$JAVA_HOME\jmods" \
   --compress 2 \
   --no-header-files \
   --no-man-pages \
   --add-modules java.base,java.sql,openj9.jvm,openj9.sharedclasses,jdk.net,java.naming,java.management,jdk.unsupported,java.desktop,java.scripting,java.rmi \
   --strip-debug \
   --output "/opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}"
mkdir -p "/opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}/jitcache"

cp /home/jenkins/cynergi-middleware/support/deployment/cynergi-middleware.httpd.conf /opt/cyn/v01/cynmid/cynergi-middleware.httpd.conf
sed "s/@@JAVA_VER_BUILD@@/${VER_BUILD}/g; s/@@MICRONAUT_ENV@@/${MICRONAUT_ENV}/g" /home/jenkins/cynergi-middleware/support/deployment/cynergi-middleware.conf > /opt/cyn/v01/cynmid/cynergi-middleware.conf
sed "s/@@JAVA_VER_BUILD@@/${VER_BUILD}/g; s/@@GROOVY_VER@@/${GROOVY_VERSON}/g" /home/jenkins/cynergi-middleware/support/deployment/groovy-proxy.sh > /opt/cyn/v01/cynmid/groovy/bin/groovy
chmod u+x /opt/cyn/v01/cynmid/groovy/bin/groovy
cp /home/jenkins/cynergi-middleware/support/development/cynergidb/setup-database.sql /opt/cyn/v01/cynmid/data/
cp /home/jenkins/cynergi-middleware/${buildPath}/${jarFile} /opt/cyn/v01/cynmid/${jarFile}
mkdir -p "/opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}/jitcache"
tar -c --owner=0 --group=0 --to-stdout /opt/cyn | xz -6 - > "${buildPath}/${deployFile}"

cat <<EOF
buildPath=${buildPath}
deployFile=${deployFile}
jarFile=${jarFile}
EOF
