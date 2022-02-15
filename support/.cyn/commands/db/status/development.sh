#!/usr/bin/env bash
## description: displays the status of the cynergidb service

cd ../development || exit 3

if [ -z `docker-compose ps -q cynergidb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergidb)` ]; then
  echo "cynergidb is not running"
  exit 1
else
  echo "cynergidb services is running, but aren't accessible yet"
  exit 2
fi
