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
   --add-modules java.base,java.compiler,java.datatransfer,java.desktop,java.instrument,java.logging,java.management,java.management.rmi,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.se,java.security.jgss,java.security.sasl,java.smartcardio,java.sql,java.sql.rowset,java.transaction.xa,java.xml,java.xml.crypto,jdk.accessibility,jdk.attach,jdk.charsets,jdk.compiler,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.dynalink,jdk.editpad,jdk.internal.ed,jdk.internal.jvmstat,jdk.internal.le,jdk.internal.opt,jdk.jcmd,jdk.jdeps,jdk.jdi,jdk.jdwp.agent,jdk.jsobject,jdk.localedata,jdk.management,jdk.management.agent,jdk.naming.dns,jdk.naming.ldap,jdk.naming.rmi,jdk.net,jdk.pack,jdk.rmic,jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported,jdk.unsupported.desktop,jdk.xml.dom,jdk.zipfs,openj9.dataaccess,openj9.dtfj,openj9.dtfjview,openj9.gpu,openj9.jvm,openj9.sharedclasses,openj9.traceformat \
   --strip-debug \
   --output "/opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}"
mkdir -p "/opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}/jitcache"

cp /home/jenkins/cynergi-middleware/support/deployment/cynergi-middleware.httpd.conf /opt/cyn/v01/cynmid/cynergi-middleware.httpd.conf
ln -s /opt/cyn/v01/cynmid/java/openj9/${VER_BUILD} /opt/cyn/v01/cynmid/java/current
ln -s /opt/cyn/v01/cynmid/groovy/${GROOVY_VERSION} /opt/cyn/v01/cynmid/groovy/current
#sed's 1a command means to append the following text after line 1 of the file
sed "s/@@JAVA_VER_BUILD@@/${VER_BUILD}/g; s/@@MICRONAUT_ENV@@/${MICRONAUT_ENV}/g" /home/jenkins/cynergi-middleware/support/deployment/cynergi-middleware.conf > /opt/cyn/v01/cynmid/cynergi-middleware.conf
sed -i '1a export JAVA_HOME=/opt/cyn/v01/cynmid/java/current' /opt/cyn/v01/cynmid/groovy/current/bin/startGroovy
sed -i '2a export GROOVY_HOME=/opt/cyn/v01/cynmid/groovy/current' /opt/cyn/v01/cynmid/groovy/current/bin/startGroovy
cp /home/jenkins/cynergi-middleware/support/development/cynergidb/setup-database.sql /opt/cyn/v01/cynmid/data/
cp /home/jenkins/cynergi-middleware/${buildPath}/${jarFile} /opt/cyn/v01/cynmid/${jarFile}
mkdir -p "/opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}/jitcache"
tar -c --owner=0 --group=0 --to-stdout /opt/cyn | xz -6 - > "${buildPath}/${deployFile}"

cat <<EOF
buildPath=${buildPath}
deployFile=${deployFile}
jarFile=${jarFile}
EOF
