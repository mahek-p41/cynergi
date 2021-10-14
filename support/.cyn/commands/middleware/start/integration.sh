#!/usr/bin/env bash
## description: runs cynergi middleware in integration mode

./commands/db/start/test.sh

if [ $? -eq 0 ]; then
  pushd ../../

  ./gradlew clean shadowjar

  popd

  pushd ../development

  if [ -z `docker-compose ps -q cynmidintegration` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynmidintegration)` ]; then
    docker rm -f cynmidintegration >> /tmp/cynergi-dev.log 2>&1
    docker-compose up -d --no-deps cynmidintegration
    exit $?
  else
    echo "cynergi-middleware is already running in integration mode"
    echo "can be accessed at $(docker-compose port cynmidintegration 8080)"
    exit 1
  fi
else
  echo "Database did not successfully start, not starting middleware"
  exit 1
fi
