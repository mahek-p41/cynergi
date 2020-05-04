FROM adoptopenjdk:8u252-b09-jdk-openj9-0.20.0

COPY build-run.sh /root/build-run.sh
RUN chmod u+x /root/build-run.sh
RUN mkdir -p /home/jenkins/.gradle/
RUN echo "org.gradle.daemon=false" >| /home/jenkins/.gradle/gradle.properties

VOLUME /tmp/cynergi-middleware
EXPOSE 8080
