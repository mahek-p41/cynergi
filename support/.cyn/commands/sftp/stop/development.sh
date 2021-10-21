#!/usr/bin/env bash
## description: stops the sftp dev server

pushd ../development

if [ -z `docker-compose ps -q sftpdev` ] || [ -z `docker ps -q --no-trunc | grep $(docker-compose ps -q sftpdev)` ]; then
  echo "sftp dev server is not running"
  exit 1
else
  echo "stopping sftp dev server"
  docker-compose stop sftpdev && docker-compose rm -f sftpdev
  exit $?
fi

popd
