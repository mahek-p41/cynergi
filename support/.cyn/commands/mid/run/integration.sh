#!/usr/bin/env bash
## description: runs cynergi middleware in integration mode

./commands/db/start/test.sh
cd ../../

./gradlew clean shadowjar

java -Dmicronaut.environments=integration -jar ./build/libs/cynergi-middleware-*-all.jar
