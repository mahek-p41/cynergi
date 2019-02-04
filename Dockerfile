FROM adoptopenjdk/openjdk8-openj9
COPY build/libs/*-all.jar cynergi-middleware.jar
CMD java ${JAVA_OPTS} -jar cynergi-middleware.jar
# finish code for building and deploying tarball of JRE and jar with upstart script, need to use versioning from gradle.properties
