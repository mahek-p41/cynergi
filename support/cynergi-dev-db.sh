#!/usr/bin/env sh

cd docker

docker-compose rm -f && docker-compose build cynergidevdb && docker-compose up cynergidevdb
