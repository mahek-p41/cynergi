#!/usr/bin/env bash

export USER_ID=`id -u`
export GROUP_ID=`cut -d: -f3 < <(getent group ${USER})`

docker rmi deployment_cynmid
docker-compose rm -f
docker-compose build --build-arg USER_ID=${USER_ID} --build-arg GROUP_ID=${GROUP_ID}  cynmid
docker-compose run --rm cynmid
