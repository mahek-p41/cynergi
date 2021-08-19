#!/usr/bin/env bash
## description: allows for the tailing of the cynmid process stdout

cd ../development

if [ -z `docker-compose ps -q cynmid` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynmid)` ]; then
  echo "cynmid is not running! Not tailing!"
  exit 1
else
  echo "Tailing cynmid"
  docker-compose logs -f --tail 100 cynmid
  exit $?
fi
