#!/usr/bin/env bash

if [[ -f /tmp/dumps/cynergidb.dump ]]; then
  pushd /tmp/dumps/

  echo "Restoring cynergidb from snapshot"
  dropdb --if-exists cynergidb
  createdb cynergidb

  #pg_restore -l /tmp/dumps/cynergidb.dump /tmp/tables.list # filter out flyway tables
  pg_restore --verbose --no-owner --no-privileges --username "cynergiuser" --dbname "cynergidb" --host cynergidb --port 5432 --clean /tmp/dumps/cynergidb.dump
  echo "Finished restoring cynergidb from snapshot"

  /opt/scripts/migratedb.sh
  popd
else
    echo "Loading cynergidb data from CSV"
pushd /tmp/dumps/cynergidb

/opt/scripts/migratedb.sh

for i in $(ls *.csv | sort -n); do
  TABLE_NAME=$(echo $i | cut -d'.' -f1 | cut -c7-)

psql -v ON_ERROR_STOP=1 --username "cynergiuser" --dbname "cynergidb" --host cynergidb --port 5432 <<-EOSQL
   COPY $TABLE_NAME FROM '/tmp/dumps/cynergidb/$i' DELIMITER ',' CSV HEADER;
EOSQL

done;

popd

fi
