#!/usr/bin/env bash

cd development

docker-compose rm -f && docker-compose build cynergidevdbpsql && docker-compose run --rm cynergidevdbpsql
