#!/usr/bin/env sh

cd development

docker-compose rm -f cynergidb

docker-compose stop cynergidb && docker-compose rm -f && docker-compose build cynergidb && docker-compose up cynergidb

docker-compose stop cynergidb
