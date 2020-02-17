#!/usr/bin/env bash

cd development

docker-compose rm -f && docker-compose build cynergidbpsql && docker-compose run --rm cynergidbpsql
