FROM docker.io/bellsoft/liberica-openjdk-debian:11.0.13

ARG USER_ID=1001
ARG GROUP_ID=1001

RUN mkdir -p /opt/cynergi-middleware

RUN groupadd --system --gid ${GROUP_ID} jenkins
RUN useradd --system --gid jenkins --uid ${USER_ID} --shell /bin/bash --create-home jenkins
RUN mkdir -p /home/jenkins/.gradle/
RUN echo "org.gradle.daemon=false" >| /home/jenkins/.gradle/gradle.properties
RUN chown -R jenkins:jenkins /home/jenkins/.gradle/

COPY ./ /opt/cynergi-middleware
RUN chown -R jenkins:jenkins /opt/cynergi-middleware/

WORKDIR /opt/cynergi-middleware

USER jenkins

ENTRYPOINT ./gradlew --no-daemon --info clean && ./gradlew --no-daemon --info shadowJar && java -Dmicronaut.environments=development -jar /opt/cynergi-middleware/build/libs/cynergi-middleware.jar
