#!/usr/bin/env sh

cd development

docker-compose rm -f cynmid

docker-compose stop cynmid && docker-compose rm -f && docker-compose build cynmid && docker-compose up cynmid

docker-compose stop cynmid
