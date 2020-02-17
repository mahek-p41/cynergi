#!/usr/bin/env bash

cd development

docker-compose rm -f && docker-compose build cynergitestdbpsql && docker-compose run --rm cynergitestdbpsql
