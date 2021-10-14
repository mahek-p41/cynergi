#!/usr/bin/env bash
## description: starts the cynergidb and the fastinfo_production databases and makes them available on localhost:6432 and returns once they are up and running

cd ../development

if [ -z `docker-compose ps -q cynergitestdb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergitestdb)` ]; then
  docker rm cynergitestdb >> /tmp/cynergi-dev.log 2>&1
  docker-compose build --force-rm --quiet cynergibasedb
  docker-compose build --force-rm --quiet cynergitestdb
  docker-compose up -d --no-deps cynergitestdb
  if [ $? -eq 0 ]; then
    docker-compose build --force-rm --quiet cynergitestdbready && docker-compose run --rm cynergitestdbready
  fi
  exit $?
else
  echo "cynergitestdb is already running checking if it is accepting connections"
  docker-compose build --force-rm --quiet cynergitestdbready && docker-compose run --rm cynergitestdbready
  echo "can be accessed at $(docker-compose port cynergitestdb 5432)"
  exit 0
fi
