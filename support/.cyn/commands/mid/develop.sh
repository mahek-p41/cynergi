#!/usr/bin/env bash
## description: runs cynergi middleware in integration mode

./commands/db/start/development.sh
cd ../../

./gradlew clean shadowjar

java -Dmicronaut.environments=develop -jar ./build/libs/cynergi-middleware-*-all.jar
