#!/usr/bin/env bash

cd development

docker-compose down
docker-compose rm -f -v
docker rmi docker_cynergidevdb docker_cynergitestdb docker_cynergidevdbsnapshot docker_cynergidevdbpsql
