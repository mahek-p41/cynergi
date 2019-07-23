#!/usr/bin/env sh

cd docker

docker-compose stop cynergitestdb
docker-compose rm -f cynergitestdb

docker-compose build cynergitestdb && docker-compose up cynergitestdb

docker-compose stop cynergitestdb
