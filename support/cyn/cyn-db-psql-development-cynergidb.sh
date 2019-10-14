#!/usr/bin/env sh
## description: connect a psql prompt to the cynergidb

cd development

if [ -z `docker-compose ps -q cynergidb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergidb)` ]; then
  echo "cynergidb is not running! Not connecting psql."
  exit 1
else
  echo "Connecting interactive psql to cynergidb"
  docker-compose build cynergidbpsql && docker-compose run --rm cynergidbpsql
  exit $?
fi
