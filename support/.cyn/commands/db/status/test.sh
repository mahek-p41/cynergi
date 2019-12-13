#!/usr/bin/env bash
## description: displays the status of the cynergidb service

cd ../development || exit 3

if [ -z `docker-compose ps -q cynergitestdb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergitestdb)` ]; then
  echo "cynergitestdb is not running"
  exit 1
elif [ -z `docker-compose run --rm cynergitestdbready` ]; then
  echo "cynergitestdb is running"
  echo "can be accessed at $(docker-compose port cynergitestdb 5432)"
  exit 0
else
  echo "cynergitestdb is running, but is not accessable yet"
  exit 2
fi
