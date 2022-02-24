#!/usr/bin/env sh
## description: migrate via flyway the cynergitestdb database

cd ../development

if [ -z `docker-compose ps -q cynergitestdb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergitestdb)` ]; then
  echo "cynergitestdb is not running! Not connecting psql."
  exit 1
else
  echo "Migration Test database"
  cd ../../
  ./support/development/cynergidb/migratedb.groovy -d cynergitestdb -H localhost -P 7432 -u postgres -p password -m ${PWD}/src/main/resources/db/migration/postgres/
  exit $?
fi
