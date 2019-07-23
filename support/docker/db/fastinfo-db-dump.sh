#!/usr/bin/env sh

mkdir -p /tmp/dumps
pg_dump -Fc -v --host "cynergidevdb" --username="postgres" --file=/tmp/dumps/fastinfo.dump fastinfo_production
