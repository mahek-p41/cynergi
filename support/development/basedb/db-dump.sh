#!/usr/bin/env sh

mkdir -p /tmp/dumps
pg_dump -Fc -v --host "$1" --username="postgres" --file=/tmp/dumps/$2.dump $2
