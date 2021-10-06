#!/usr/bin/env bash
## description: starts the cynergidb and the fastinfo_production databases and makes them available on localhost:6432 and returns once they are up and running

cd ../development

if [ -f "./db/DatabaseDumps/cynergidb.dump" ]; then
  if [ -f "./db/DatabaseDumps/fastinfo.dump" ]; then
    if [ -z `docker-compose ps -q cynergidb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergidb)` ]; then
      docker rm -f cynergidb > /dev/null 2>&1
      docker-compose build --force-rm --quiet cynergibasedb
      docker-compose build --force-rm --quiet cynergidb
      docker-compose up -d --no-deps cynergidb
      if [ $? -eq 0 ]; then
        docker-compose build --force-rm --quiet cynergidbready && docker-compose run --rm cynergidbready
      fi
      exit $?
    else
      echo "cynergidb are already running checking if it is accepting connections"
      docker-compose build cynergidbready && docker-compose run --rm cynergidbready
      echo "can be accessed at $(docker-compose port cynergidb 5432)"
      exit 0
    fi
  else
    echo "support/development/db/DatabaseDumps/fastinfo.dump is missing!!"
    exit 1
  fi
else
  echo "support/development/db/DatabaseDumps/cynergidb.dump is missing!!"
  exit 1
fi
