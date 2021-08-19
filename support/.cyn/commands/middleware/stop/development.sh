#!/usr/bin/env bash
## description: stops the cynmid service

cd ../development

if [ -z `docker-compose ps -q cynmid` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynmid)` ]; then
  echo "cynmid is not running"
  exit 1
else
  echo "stopping cynmid"
  docker-compose stop cynmid && docker-compose rm -f cynmid
  exit $?
fi
