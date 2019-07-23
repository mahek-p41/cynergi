FROM postgres:9.3.25-alpine

COPY .pgpass /root/.pgpass
COPY fastinfo-db-dump.sh /root/fastinfo-db-dump.sh
RUN chmod 0600 /root/.pgpass
RUN chmod u+x /root/fastinfo-db-dump.sh
RUN dos2unix /root/fastinfo-db-dump.sh

VOLUME /tmp/dumps
CMD [ /usr/local/bin/psql", "--host=pg", "--port=5432", "postgres", "postgres" ]
