#!/usr/bin/env bash
## description: allows for the tailing of the cynergitestdb process stdout

cd ../development

if [ -z `docker-compose ps -q cynergitestdb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergitestdb)` ]; then
  echo "cynergitestdb is not running! Not tailing."
  exit 1
else
  echo "Tailing cynergitestdb"
  docker-compose logs -f --tail 100 cynergitestdb
  exit $?
fi
