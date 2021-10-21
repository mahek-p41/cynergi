#!/usr/bin/env bash
## description: cleans up the docker hosted services and volumes

cd ../development

rm -rf /tmp/sftpuser/

docker-compose down --rmi all --volumes
