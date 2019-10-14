#!/usr/bin/env bash

set -o errexit -o pipefail -o noclobber -o nounset

VER_BUILD=$(java -version 2>&1 | awk '/build/ {gsub("\)","") ; print $NF}' | head -n 1)

mkdir -p /opt/cyn/v01/cynmid/logs/
mkdir -p /opt/cyn/v01/cynmid/java/openj9/"${VER_BUILD}"/jitcache
export POSTGRES_REACTIVE_CLIENT_PORT=5432
export POSTGRES_REACTIVE_CLIENT_HOST=cynergitestdeploydb
cd /home/jenkins/cynergi-middleware
./gradlew --no-daemon --stacktrace clean test jacocoTestReport buildApiDocs shadowJar

mkdir -p /opt/cyn/v01/cynmid/legacy/import/
cat > /opt/cyn/v01/cynmid/cynergi-middleware.conf <<EOF
author "High Touch Technologies"
description "upstart script for cynergi-middleware"

# respawn the job up to 5 times within a 10 second period.
# If the job exceeds these values, it will be stopped and
# marked as failed.
respawn
respawn limit 3 60

#start on filesystem or runlevel [2345]
start on started postgresql-9.3
stop on shutdown

script
 JAVA_MEM_OPTS="-Xms10m -Xmx64m"
 JAVA_GC_OPTS="-Xgcpolicy:gencon"
 JAVA_SYS_PROPS="-Dmicronaut.environments=prod -Dapp.name=cynergi-middleware -Djava.awt.headless=true"
 JAVA_JIT_OPTS="-Xshareclasses:cacheDir=/opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}/jitcache"
 # construct the java command, configured to use the prod profile
 exec /opt/cyn/v01/cynmid/java/openj9/${VER_BUILD}/bin/java \$JAVA_MEM_OPTS \$JAVA_GC_OPTS \$JAVA_JIT_OPTS \$JAVA_SYS_PROPS -jar /opt/cyn/v01/cynmid/cynergi-middleware.jar
end script

EOF

cat > /opt/cyn/v01/cynmid/cynergi-middleware.httpd.conf <<EOF
# proxy configuration for the cynergi-middleware API
ProxyPass        "/api/" "http://127.0.0.1:10900/api/"
ProxyPassReverse "/api/" "http://127.0.0.1:10900/api/"

EOF

mkdir -p /opt/cyn/v01/cynmid/data/
cp /home/jenkins/cynergi-middleware/support/deployment/setup-database.sql /opt/cyn/v01/cynmid/data/
cp /home/jenkins/cynergi-middleware/build/libs/cynergi-middleware*-all.jar /opt/cyn/v01/cynmid/cynergi-middleware.jar
tar -c --owner=0 --group=0 --to-stdout /opt/cyn | xz -6 - > /home/jenkins/cynergi-middleware/build/libs/cynergi-middleware.tar.xz
