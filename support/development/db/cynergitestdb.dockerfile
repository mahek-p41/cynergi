FROM postgres:9.3.25-alpine

COPY cynergi-inittestdb.sh /docker-entrypoint-initdb.d/initdb.sh
RUN dos2unix /docker-entrypoint-initdb.d/initdb.sh

COPY cynergi-inittestdb.sql /docker-entrypoint-initdb.d/initdb.sql
RUN dos2unix /docker-entrypoint-initdb.d/initdb.sql

COPY cynergi-test-db-ready.sh /tmp/cynergi-test-db-ready.sh
RUN dos2unix /tmp/cynergi-test-db-ready.sh

COPY DatabaseDumps/test-inventory.csv /tmp/test-inventory.csv
RUN dos2unix /tmp/test-inventory.csv

COPY DatabaseDumps/test-itemfile.csv /tmp/test-itemfile.csv
RUN dos2unix /tmp/test-itemfile.csv
