#!/usr/bin/env bash

cd docker

docker-compose rm -f && docker-compose build fastinfosnapshot && docker-compose run --rm fastinfosnapshot
