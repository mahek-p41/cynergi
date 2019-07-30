FROM postgres:9.3.25-alpine

COPY pgpass /root/.pgpass
COPY psqlrc /root/.psqlrc
COPY cynergi-dev-db-dump.sh /root/cynergi-dev-db-dump.sh
RUN chmod 0600 /root/.pgpass
RUN chmod u+x /root/cynergi-dev-db-dump.sh
RUN dos2unix /root/cynergi-dev-db-dump.sh
RUN apk add pspg

VOLUME /tmp/dumps
CMD [ /usr/local/bin/psql", "--host=pg", "--port=5432", "postgres", "postgres" ]
