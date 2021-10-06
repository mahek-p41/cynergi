#!/usr/bin/env bash
## description: stops the cynmidintegration service

cd ../development

if [ -z `docker-compose ps -q cynmidintegration` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynmidintegration)` ]; then
  echo "cynmidintegration is not running"
  exit 1
else
  echo "stopping cynmidintegration"
  docker-compose stop cynmidintegration && docker-compose rm -f cynmidintegration
  exit $?
fi
