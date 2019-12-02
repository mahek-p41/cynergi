#!/usr/bin/env sh
./gradlew clean shadowJar && java -Dmicronaut.environments=develop -jar ./build/libs/cynergi-middleware-r2-all.jar
#java -Dmicronaut.environments=develop -jar ./build/libs/cynergi-middleware-*-all.jar
