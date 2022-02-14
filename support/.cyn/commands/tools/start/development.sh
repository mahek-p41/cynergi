#!/usr/bin/env bash

./cyn db start development
./cyn sftp start development

if [[ "$1$2" != "withoutmid" ]]; then
  echo "Starting cynergi-middleware"
  ./commands/middleware/start/development.sh
else
  echo "Not starting cynergi-middleware"
fi
