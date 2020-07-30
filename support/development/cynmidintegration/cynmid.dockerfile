FROM adoptopenjdk:8u262-b10-jdk-openj9-0.21.0-bionic

COPY build-run.sh /root/build-run.sh
RUN chmod u+x /root/build-run.sh
RUN mkdir -p /home/jenkins/.gradle/
RUN echo "org.gradle.daemon=false" >| /home/jenkins/.gradle/gradle.properties

VOLUME /tmp/cynergi-middleware
EXPOSE 8080
