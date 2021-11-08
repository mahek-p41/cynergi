FROM cynergibasedb

COPY cynergi-initdb.sh /docker-entrypoint-initdb.d/cynergi-initdb.sh
COPY setup-database.sql /tmp/setup-database.sql
RUN dos2unix /docker-entrypoint-initdb.d/cynergi-initdb.sh
RUN dos2unix /tmp/setup-database.sql

COPY export-cynergidb.groovy /opt/scripts
COPY export-fastinfo.groovy /opt/scripts
COPY migratedb.groovy /opt/scripts
COPY truncatedb.groovy /opt/scripts
COPY truncatedb.sh /opt/scripts
COPY load-dev-fastinfo-data.sh /opt/scripts
COPY load-dev-cynergidb-data.sh /opt/scripts
COPY setup-database.sh /opt/scripts
COPY migratedb.sh /opt/scripts

RUN chmod u+x /opt/scripts/*.sh &&\
    chmod u+x /opt/scripts/*.groovy

VOLUME /tmp/dumps
