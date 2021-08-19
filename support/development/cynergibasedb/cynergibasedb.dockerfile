FROM postgres:12.5-alpine

COPY pgpass /root/.pgpass
RUN chmod 0600 /root/.pgpass

COPY psqlrc /root/.psqlrc

COPY db-ready.sh /tmp/db-ready.sh
RUN chmod a+x /tmp/db-ready.sh

COPY db-dump.sh /tmp/db-dump.sh
RUN chmod u+x /tmp/db-dump.sh

RUN apk update && apk add pspg
