FROM alpine:3.8
RUN apk update && apk add postgresql-client
RUN mkdir -p /tmp/dumps

COPY .pgpass /root/.pgpass
COPY cynergi-dev-db-dump.sh /root/cynergi-dev-db-dump.sh
RUN chmod 0600 /root/.pgpass
RUN chmod u+x /root/cynergi-dev-db-dump.sh
RUN dos2unix /root/cynergi-dev-db-dump.sh

VOLUME /tmp/dumps
CMD [ "/bin/ash" ]