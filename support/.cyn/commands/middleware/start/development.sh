#!/usr/bin/env bash
## description: runs cynergi middleware in develop mode

./commands/db/start/development.sh

if [ $? -eq 0 ]; then
  pushd ../../

  ./gradlew clean shadowjar

  popd

  pushd ../development

  if [ -z `docker-compose ps -q cynmid` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynmid)` ]; then
    docker rm -f cynmid > /dev/null 2>&1
    docker-compose up -d --no-deps cynmid
    exit $?
  else
    echo "cynergi-middleware is already running"
    echo "can be accessed at $(docker-compose port cynmid 8080)"
    exit 1
  fi
else
  echo "Database did not successfully start, not starting middleware"
  exit 1
fi
