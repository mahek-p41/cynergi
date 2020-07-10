#!/usr/bin/env sh

#sed -ri "s/#log_statement = 'none'/log_statement = 'all'/g" /var/lib/postgresql/data/postgresql.conf

dropdb --if-exists cynergidevelopdb
createdb cynergidevelopdb

dropdb --if-exists cynergidb
createdb cynergidb
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "cynergidb" <<-EOSQL
   CREATE USER cynergiuser WITH PASSWORD 'password';
   ALTER ROLE cynergiuser SUPERUSER;
EOSQL

if [[ -f /tmp/dumps/cynergidb.dump ]]; then
    echo "Restoring cynergidb from snapshot"
    pg_restore -v -O -x --role=postgres --dbname=cynergidb /tmp/dumps/cynergidb.dump
    echo "Finished restoring cynergidb from snapshot"
fi

dropdb --if-exists fastinfo_production
createdb fastinfo_production

if [[ -f /tmp/dumps/fastinfo.dump ]]; then
    echo "Restoring fastinfo_production from snapshot"
    pg_restore -v -O -x --role=postgres --dbname=fastinfo_production /tmp/dumps/fastinfo.dump
    echo "Finished restoring fastinfo_production from snapshot"
fi

psql -f /tmp/setup-database.sql -v "ON_ERROR_STOP=1" -v fastinfoUserName=postgres -v fastinfoPassword=password -v datasets=corrto,corptp,corrll,cornwv
