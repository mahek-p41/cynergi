#!/usr/bin/env sh
## description: connect a psql prompt to the cynergidevelopdb

cd development

if [ -z `docker-compose ps -q cynergidb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergidb)` ]; then
  echo "cynergidevelopdb is not running! Not connecting psql."
  exit 1
else
  echo "Connecting interactive psql to cynergidevelopdb"
  docker-compose build cynergidevelopdbpsql && docker-compose run --rm cynergidevelopdbpsql
  exit $?
fi
