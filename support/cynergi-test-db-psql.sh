#!/usr/bin/env bash

cd docker

docker-compose rm -f && docker-compose build cynergitestdbpsql && docker-compose run --rm cynergitestdbpsql