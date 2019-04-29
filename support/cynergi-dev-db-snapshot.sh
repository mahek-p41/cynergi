#!/usr/bin/env bash

cd docker

docker-compose rm -f && docker-compose build cynergidevdbsnapshot && docker-compose run --rm cynergidevdbsnapshot