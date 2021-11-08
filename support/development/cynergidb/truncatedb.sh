#!/usr/bin/env bash

/opt/groovy/bin/groovy /opt/scripts/truncatedb.groovy \
  --user cynergiuser \
  --password password \
  --host cynergidb \
  --migrations /tmp/migrations
