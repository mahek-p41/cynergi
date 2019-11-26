#!/usr/bin/env bash

cd development

docker-compose rm -f && docker-compose build cynergidbsnapshot && docker-compose run --rm cynergidbsnapshot
