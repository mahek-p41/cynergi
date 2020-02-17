#!/usr/bin/env sh

mkdir -p /tmp/dumps
pg_dump -Fc -v --host "cynergidb" --username="postgres" --file=/tmp/dumps/cynergidb.dump cynergidb
