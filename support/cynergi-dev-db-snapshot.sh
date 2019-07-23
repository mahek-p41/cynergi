#!/usr/bin/env bash

cd development

docker-compose rm -f && docker-compose build cynergidevdbsnapshot && docker-compose run --rm cynergidevdbsnapshot
