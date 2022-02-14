#!/usr/bin/env bash
## description: starts the cynergidb and the fastinfo_production databases and makes them available on localhost:6432 and returns once they are up and running

cd ../development

if [ -z `docker-compose ps -q cynergidb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergidb)` ]; then
  docker rm -f cynergidb >> /tmp/cynergi-dev.log 2>&1
  docker-compose build --force-rm cynergibasedb >> /tmp/cynergi-dev.log 2>&1
  docker-compose build --force-rm cynergidb >> /tmp/cynergi-dev.log 2>&1
  docker-compose up -d --no-deps cynergidb

  if [ $? -eq 0 ]; then
    docker exec cynergidb /opt/scripts/db-ready.sh cynergidb || exit $?

    echo "Loading fastinfo development data"
    docker exec cynergidb /opt/scripts/load-dev-fastinfo-data.sh >> /tmp/cynergi-dev.log 2>&1 || exit $?

    echo "Setting up bridge between cynergidb and fastinfo_production"
    docker exec cynergidb /opt/scripts/setup-database.sh >> /tmp/cynergi-dev.log 2>&1 || exit $?

    echo "Loading cynergidb development data"
    docker exec cynergidb /opt/scripts/load-dev-cynergidb-data.sh >> /tmp/cynergi-dev.log 2>&1 || exit $?
  fi

  exit $?
else
  echo "cynergidb are already running checking if it is accepting connections"
  docker-compose build cynergidbready && docker-compose run --rm cynergidbready
  echo "can be accessed at $(docker-compose port cynergidb 5432)"
  exit 0
fi
