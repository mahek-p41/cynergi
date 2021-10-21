#!/usr/bin/env bash
## description: runs cynergi middleware in develop mode

./commands/db/start/development.sh

if [ $? -eq 0 ]; then
  echo ""
  ./commands/sftp/start/development.sh

  if [ $? -ne 0 ]; then
    echo "SFTP Server did not start up successfully"
  fi
else
  echo "Database did not successfully start, not starting SFTP server"
  exit $?
fi
