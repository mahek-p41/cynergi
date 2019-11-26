#!/usr/bin/env bash

cd development

docker-compose rm -f && docker-compose build cynergidevelopdbpsql && docker-compose run --rm cynergidevelopdbpsql
