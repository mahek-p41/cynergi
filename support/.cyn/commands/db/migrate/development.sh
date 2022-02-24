#!/usr/bin/env sh
## description: migrate via flyway the cynergitestdb database

cd ../development

if [ -z `docker-compose ps -q cynergidb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergidb)` ]; then
  echo "cynergidb is not running! Not connecting psql."
  exit 1
else
  echo "Migrating cynergidb"
  cd ../../
  ./support/development/cynergidb/migratedb.groovy -d cynergidb -H localhost -P 6432 -u cynergiuser -p password -m ${PWD}/src/main/resources/db/migration/postgres/
  exit $?
fi
