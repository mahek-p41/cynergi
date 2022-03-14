#!/usr/bin/env sh
## description: migrate via flyway the cynergitestdb database

cd ../development

if [ -z `docker-compose ps -q cynergidb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergidb)` ]; then
  echo "cynergidb is not running! Not connecting psql."
  exit 1
else
  echo "Migrating cynergidb"
  docker-compose run --rm cynergidbmigrate
  exit $?
fi
