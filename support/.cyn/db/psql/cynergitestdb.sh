#!/usr/bin/env sh
## description: connect a psql prompt to the Postgres hosted cynergidb database

cd ../development

if [ -z `docker-compose ps -q cynergitestdb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergitestdb)` ]; then
  echo "cynergidb is not running! Not connecting psql."
  exit 1
else
  echo "Connecting interactive psql to cynergidb"
  docker-compose build cynergitestdbpsql && docker-compose run --rm cynergitestdbpsql
  exit $?
fi
