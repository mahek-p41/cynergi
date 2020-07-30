#!/usr/bin/env bash

export USER_ID=$(id -u)
export GROUP_ID=$(cut -d: -f3 < <(getent group "${USER}"))

mkdir -p gradleCache
mkdir -p gradleWrapper
docker rmi deployment_cynmid
docker-compose build cynergipgdb
docker-compose build --build-arg USER_ID="${USER_ID}" --build-arg GROUP_ID="${GROUP_ID}" cynmid
docker-compose run --rm cynmid
