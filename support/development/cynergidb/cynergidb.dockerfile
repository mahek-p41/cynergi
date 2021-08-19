FROM cynergibasedb

COPY cynergi-initdb.sh /docker-entrypoint-initdb.d/initdb.sh
COPY setup-database.sql /tmp/setup-database.sql
RUN dos2unix /docker-entrypoint-initdb.d/initdb.sh
RUN dos2unix /tmp/setup-database.sql

VOLUME /tmp/dumps
