#!/usr/bin/env bash
## description: runs cynergi middleware in develop mode

set -e

cd ../development

if [ -z `docker-compose ps -q cynmid` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynmid)` ]; then
  docker rm -f cynmid >> /tmp/cynergi-dev.log 2>&1
  docker-compose build --force-rm cynmid >> /tmp/cynergi-dev.log 2>&1
  docker-compose up --no-deps -d cynmid

  exit $?
else
  echo "cynmid is already running and can be accessed at $(docker-compose port cynmid 8080)"
  exit 0
fi
