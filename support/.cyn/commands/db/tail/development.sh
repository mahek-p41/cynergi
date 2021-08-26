#!/usr/bin/env bash
## description: allows for the tailing of the cynergidb process stdout

cd ../development

if [ -z `docker-compose ps -q cynergidb` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q cynergidb)` ]; then
  echo "cynergidb is not running! Not tailing!"
  exit 1
else
  echo "Tailing cynergidb"
  docker-compose logs -f --tail 100 cynergidb
  exit $?
fi
