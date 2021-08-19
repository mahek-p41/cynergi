#!/usr/bin/env bash
## description: allows for the tailing of the cynmidintegration process stdout

cd ../development

if [ -z `docker-compose ps -q cynmidintegration` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynmidintegration)` ]; then
  echo "cynmidintegration is not running! Not tailing."
  exit 1
else
  echo "Tailing cynmidintegration"
  docker-compose logs -f --tail 100 cynmidintegration
  exit $?
fi
