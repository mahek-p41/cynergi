#!/usr/bin/env sh

sed -ri "s/#log_statement = 'none'/log_statement = 'all'/g" /var/lib/postgresql/data/postgresql.conf

dropdb --if-exists cynergidemodb
createdb cynergidemodb

dropdb --if-exists cynergidb
createdb cynergidb

if [[ -f /tmp/dumps/cynergidb.dump ]]; then
    echo "Restoring cynergidb from snapshot"
    pg_restore -v -O -x --role=postgres --dbname=cynergidb /tmp/dumps/cynergidb.dump
    echo "Finished restoring cynergidb from snapshot"
fi

dropdb --if-exists customers
createdb customers

if [[ -f /tmp/dumps/customers.dump ]]; then
    echo "Restoring customers from snapshot"
    pg_restore -v -O -x --role=postgres --dbname=customers /tmp/dumps/customers.dump
    echo "Finished restoring customers from snapshot"
fi

dropdb --if-exists transactions
createdb transactions

if [[ -f /tmp/dumps/transactions.dump ]]; then
    echo "Restoring transactions from snapshot"
    pg_restore -v -O -x --role=postgres --dbname=transactions /tmp/dumps/transactions.dump
    echo "Finished restoring transactions from snapshot"
fi

dropdb --if-exists notifications
createdb notifications

if [[ -f /tmp/dumps/notifications.dump ]]; then
    echo "Restoring transactions from snapshot"
    pg_restore -v -O -x --role=postgres --dbname=notifications /tmp/dumps/notifications.dump
    echo "Finished restoring transactions from snapshot"
fi
