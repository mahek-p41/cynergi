#!/usr/bin/env sh

cd docker

docker-compose rm -f && docker-compose build cynergitestdb && docker-compose up cynergitestdb
