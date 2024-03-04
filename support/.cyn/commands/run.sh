#!/usr/bin/env bash
## description: runs the build command from the readme
cd ../..
./gradlew clean shadowJar && java -Dmicronaut.environments=development -jar ./build/libs/cynergi-middleware.jar
