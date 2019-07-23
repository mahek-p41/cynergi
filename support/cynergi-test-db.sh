#!/usr/bin/env sh

cd development

docker-compose stop cynergitestdb
docker-compose rm -f cynergitestdb

docker-compose build cynergitestdb && docker-compose up cynergitestdb

docker-compose stop cynergitestdb
