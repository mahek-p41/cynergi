FROM postgres:9.3.25-alpine

COPY cynergi-inittestdb.sh /docker-entrypoint-initdb.d/initdb.sh
RUN dos2unix /docker-entrypoint-initdb.d/initdb.sh

COPY cynergi-inittestdb.sql /docker-entrypoint-initdb.d/initdb.sql
RUN dos2unix /docker-entrypoint-initdb.d/initdb.sql

COPY cynergi-test-db-ready.sh /tmp/cynergi-test-db-ready.sh
RUN dos2unix /tmp/cynergi-test-db-ready.sh

