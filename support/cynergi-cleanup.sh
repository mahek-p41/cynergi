#!/usr/bin/env bash

cd development

docker-compose down
docker-compose rm -f -v
docker rmi docker_cynergidb docker_cynergitestdb docker_cynergidbsnapshot docker_cynergidbpsql
