#!/usr/bin/env sh

cd development

docker-compose rm -f cynergidevdb

docker-compose stop cynergidevdb && docker-compose rm -f && docker-compose build cynergidevdb && docker-compose up cynergidevdb

docker-compose stop cynergidevdb
