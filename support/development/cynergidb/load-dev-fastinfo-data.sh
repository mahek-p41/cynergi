#!/usr/bin/env bash

if [[ -f /tmp/dumps/fastinfo.dump ]]; then
    echo "Restoring fastinfo_production from snapshot"
    pg_restore -v -O -x --username "fastinfo_dba" --host=cynergidb --dbname "fastinfo_production" --port 5432 /tmp/dumps/fastinfo.dump
    echo "Finished restoring fastinfo_production from snapshot"
else
  echo "Loading fastinfo data from CSV"
  pushd /tmp/dumps/fastinfo

  psql -v ON_ERROR_STOP=1 --username "fastinfo_dba" --host=cynergidb --dbname "fastinfo_production" --port 5432 -f schema-create.sql

psql -v ON_ERROR_STOP=1 --username "fastinfo_dba" --host=cynergidb --dbname "fastinfo_production" --port 5432 <<-EOSQL
   CREATE SCHEMA IF NOT EXISTS public;
EOSQL

  for i in $(ls *.sql | grep -v "schema-create.sql" | sort -u); do
    psql -v ON_ERROR_STOP=1 --username "fastinfo_dba" --host=cynergidb --dbname "fastinfo_production" --port 5432 -f $i
  done;

  for i in $(ls *.csv | sort -n); do
    TABLE_NAME="$(echo $i | cut -d'.' -f1).$(echo $i | cut -d'.' -f2)"
psql -v ON_ERROR_STOP=1 --username "fastinfo_dba" --host=cynergidb --dbname "fastinfo_production" --port 5432 <<-EOSQL
   COPY $TABLE_NAME FROM '/tmp/dumps/fastinfo/$i' DELIMITER ',' CSV HEADER;
EOSQL
  done;

  popd
fi
