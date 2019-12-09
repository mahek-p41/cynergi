#!/usr/bin/env bash
## description: stops the cynergidb service

cd ../development

if [ -z `docker-compose ps -q cynergitestdb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergitestdb)` ]; then
  echo "cynergitestdb is not running"
  exit 1
else
  echo "stopping cynergitestdb"
  docker-compose stop cynergitestdb && docker-compose rm -f cynergitestdb
  exit $?
fi
