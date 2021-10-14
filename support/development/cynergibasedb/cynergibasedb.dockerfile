ARG GROOVY_VER=3.0.9
FROM groovy:${GROOVY_VER}-jdk11 AS groovyImage

FROM postgres:12.7-buster

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update &&\
    apt-get install pspg  dos2unix -y

COPY pgpass /root/.pgpass
COPY psqlrc /root/.psqlrc

RUN mkdir -p /opt/scripts

COPY db-ready.sh /opt/scripts/db-ready.sh
COPY db-dump.sh /opt/scripts/db-dump.sh

RUN chmod 0600 /root/.pgpass &&\
    chmod a+x /opt/scripts/db-ready.sh &&\
    chmod u+x /opt/scripts/db-dump.sh &&\
    chmod u+x /opt/scripts/* &&\
    dos2unix /opt/scripts/*

COPY --from=groovyImage /opt /opt

ENV JAVA_HOME /opt/java/openjdk
ENV GROOVY_HOME /opt/groovy

