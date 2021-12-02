#!/usr/bin/env bash

sed -ri "s/#log_statement = 'none'/log_statement = 'all'/g" /var/lib/postgresql/data/postgresql.conf

dropdb --if-exists fastinfo_production
createdb fastinfo_production

dropdb --if-exists cynergidb
createdb cynergidb

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "fastinfo_production" <<-EOSQL
   CREATE USER fastinfo_dba WITH PASSWORD 'password';
   ALTER ROLE fastinfo_dba SUPERUSER;
   CREATE ROLE fastinfo_app_role WITH
     NOLOGIN
     NOSUPERUSER
     INHERIT
     NOCREATEDB
     NOCREATEROLE
     NOREPLICATION;
   CREATE ROLE fastinfo_query_role WITH
     NOLOGIN
     NOSUPERUSER
     INHERIT
     NOCREATEDB
     NOCREATEROLE
     NOREPLICATION;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "cynergidb" <<-EOSQL
   CREATE USER cynergiuser WITH PASSWORD 'password';
   ALTER ROLE cynergiuser SUPERUSER;
EOSQL
