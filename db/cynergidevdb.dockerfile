FROM postgres:9.3.25-alpine

COPY cynergi-initdb.sh /docker-entrypoint-initdb.d/initdb.sh
RUN dos2unix /docker-entrypoint-initdb.d/initdb.sh

VOLUME /tmp/dumps

