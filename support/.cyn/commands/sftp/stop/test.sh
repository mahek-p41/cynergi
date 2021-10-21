#!/usr/bin/env bash
## description: stops the sftp test server

pushd ../development > /dev/null

if [ -z `docker-compose ps -q sftptest` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q sftptest)` ]; then
  echo "sftp dev server is not running"
  exit 1
else
  echo "stopping sftp dev server"
  docker-compose stop sftptest && docker-compose rm -f sftptest
  exit $?
fi

popd > /dev/null
