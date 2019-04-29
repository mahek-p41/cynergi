#!/usr/bin/env bash

cd docker

docker-compose rm -f && docker-compose build cynergidevdbpsql && docker-compose run --rm cynergidevdbpsql