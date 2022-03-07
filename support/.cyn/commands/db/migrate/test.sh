#!/usr/bin/env sh
## description: migrate via flyway the cynergitestdb database

cd ../development

if [ -z `docker-compose ps -q cynergitestdb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergitestdb)` ]; then
  echo "cynergitestdb is not running! Not connecting psql."
  exit 1
else
  echo "Migration Test database"
  docker-compose run --rm cynergitestdbmigrate
  exit $?
fi
