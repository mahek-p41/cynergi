FROM postgres:9.3.25-alpine

COPY cynergi-inittestdb.sh /docker-entrypoint-initdb.d/initdb.sh
COPY cynergi-inittestdb.sql /docker-entrypoint-initdb.d/initdb.sql

VOLUME /tmp/fastinfo
