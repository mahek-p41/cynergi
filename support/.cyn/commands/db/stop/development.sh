#!/usr/bin/env bash
## description: stops the cynergidb service

cd ../development

if [ -z `docker-compose ps -q cynergidb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergidb)` ]; then
  echo "cynergidb and cynergidevelopdb are not running"
  exit 1
else
  echo "stopping cynergidb and cynergidevelopdb"
  docker-compose stop cynergidb && docker-compose rm -f cynergidb
  exit $?
fi
