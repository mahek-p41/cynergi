#!/usr/bin/env bash
## description: stops the cynergidb service

pushd ../development > /dev/null

if [ -z `docker-compose ps -q cynergidb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergidb)` ]; then
  echo "cynergidb is not running"
  exit 1
else
  echo "stopping cynergidb"
  docker-compose stop cynergidb && docker-compose rm -f cynergidb
  exit $?
fi

popd > /dev/null
