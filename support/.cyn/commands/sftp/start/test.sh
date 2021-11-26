#!/usr/bin/env bash
## description: runs the test sftp server

pushd ../development > /dev/null

if [ -z `docker-compose ps -q sftptest` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q sftptest)` ]; then
  echo "Starting sftp dev server"
  docker rm -f sftptest >> /dev/null 2>&1
  docker-compose build --force-rm --quiet sftptest
  mkdir -p /tmp/sftpuser/
  docker-compose up -d --no-deps sftptest
  exit $?
else
  echo "sftp dev server is already running"
  echo "can be accessed at $(docker-compose port sftptest 2223)"
  exit 1
fi

popd > /dev/null