#!/usr/bin/env bash
## description: runs the develop sftp server

pushd ../development > /dev/null

if [ -z `docker-compose ps -q sftpdev` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q sftpdev)` ]; then
  echo "Starting sftp dev server"
  docker rm -f sftpdev >> /dev/null 2>&1
  docker-compose build --force-rm --quiet sftpdev
  docker-compose up -d --no-deps sftpdev
  exit $?
else
  echo "sftp dev server is already running"
  echo "can be accessed at $(docker-compose port sftpdev 2222)"
  exit 1
fi

popd > /dev/null
