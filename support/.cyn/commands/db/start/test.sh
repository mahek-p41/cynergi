#!/usr/bin/env bash
## description: starts the cynergidb, cynergidevelopdb and the fastinfo_production databases and makes them available on localhost:6432 and returns once they are up and running

cd ../development

if [ -z `docker-compose ps -q cynergitestdb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergitestdb)` ]; then
  docker rm cynergitestdb
  docker-compose build cynergitestdb && docker-compose up -d cynergitestdb
  docker-compose build cynergitestdbready && docker-compose run --rm cynergitestdbready
  exit $?
else
  echo "cynergitestdb is already running checking if it is accepting connections"
  docker-compose build cynergitestdbready && docker-compose run --rm cynergitestdbready
  echo "can be accessed at $(docker-compose port cynergitestdb 5432)"
  exit 1
fi
