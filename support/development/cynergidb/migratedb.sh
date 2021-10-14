#!/usr/bin/env bash

/opt/groovy/bin/groovy /opt/scripts/migratedb.groovy \
  --user cynergiuser \
  --password password \
  --host cynergidb \
  --migrations /tmp/migrations
