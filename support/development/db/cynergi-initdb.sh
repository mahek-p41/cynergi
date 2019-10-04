#!/usr/bin/env sh

#sed -ri "s/#log_statement = 'none'/log_statement = 'all'/g" /var/lib/postgresql/data/postgresql.conf

dropdb --if-exists cynergidevelopdb
createdb cynergidevelopdb

dropdb --if-exists cynergidb
createdb cynergidb

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
