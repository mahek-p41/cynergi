#!/usr/bin/env bash
## description: starts the cynergidb, cynergidevelopdb and the fastinfo_production databases and makes them available on localhost:6432 and returns once they are up and running

cd ../development

if [ -z `docker-compose ps -q cynergidb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergidb)` ]; then
  docker-compose up -d --no-deps --build cynergidb
  docker-compose build cynergidbready && docker-compose run --rm cynergidbready
  exit $?
else
  echo "cynergidb and cynergidevelopdb are already running checking if it is accepting connections"
  docker-compose build cynergidbready && docker-compose run --rm cynergidbready
  echo "can be accessed at $(docker-compose port cynergidb 5432)"
  exit 1
fi
