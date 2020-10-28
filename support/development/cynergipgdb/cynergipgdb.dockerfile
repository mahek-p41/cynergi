FROM postgres:10.15-alpine

COPY pgpass /root/.pgpass
RUN chmod 0600 /root/.pgpass

COPY psqlrc /root/.psqlrc

COPY db-ready.sh /root/db-ready.sh
RUN chmod u+x /root/db-ready.sh

COPY db-dump.sh /root/db-dump.sh
RUN chmod u+x /root/db-dump.sh

RUN apk add pspg
