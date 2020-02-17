FROM postgres:9.3.25-alpine

COPY cynergi-initdb.sh /docker-entrypoint-initdb.d/initdb.sh
COPY cynergi-initdb.sql /docker-entrypoint-initdb.d/initdb.sql
COPY setup-database.sql /tmp/setup-database.sql
COPY fixup-departments.sql /tmp/fixup-departments.sql
RUN dos2unix /docker-entrypoint-initdb.d/initdb.sh
RUN dos2unix /docker-entrypoint-initdb.d/initdb.sql
RUN dos2unix /tmp/setup-database.sql

VOLUME /tmp/dumps

