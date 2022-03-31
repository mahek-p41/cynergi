#!/usr/bin/env bash

echo "Setting up the database abstraction"

# sudo -u postgres psql -f /opt/cyn/v01/cynmid/data/setup-database.sql -v "ON_ERROR_STOP=1" -v fastinfoUserName=postgres -v fastinfoPassword=password -v datasets=$(/opt/cyn/v01/cynmid/data/cyndsets-parse.sh)
export PGPASSWORD=cynergi2019 # for some reason psql when running the way it in is run in this script requires a password

psql -v ON_ERROR_STOP=1 \
  --host=localhost \
  --port 5432 \
  --username cynergiuser \
  --dbname cynergidb \
  -v fastinfoUserName=fastinfo_dba \
  -v fastinfoPassword=password \
  -v datasets=corrto,corptp,corrll,cornwv,corrdv,corapw,corrbn,coravt,corron,corfar,cortst \
  -f /tmp/setup-database.sql
