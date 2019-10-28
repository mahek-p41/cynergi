#!/usr/bin/env bash
## description: displays the status of the cynergidb service

cd ../development || exit 3

if [ -z `docker-compose ps -q cynergidb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergidb)` ]; then
  echo "cynergidb and cynergidevelopdb are not running"
  exit 1
elif [ -z `docker-compose run --rm cynergidbready` ]; then
  echo "cynergidb and cynergidevelopdb are running"
  echo "can be accessed at $(docker-compose port cynergidb 5432)"
  exit 0
else
  echo "cynergidb and cynergidevelopdb services are running, but aren't accessable yet"
  exit 2
fi
