#!/usr/bin/env bash
## description: stops all running containers

cd ../development

docker-compose rm -sf
docker-compose down


if [ -f /tmp/cynergi-dev.log ]; then
  rm /tmp/cynergi-dev.log
fi
