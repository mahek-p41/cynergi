FROM adoptopenjdk:8u262-b10-jdk-openj9-0.21.0-bionic

ARG USER_ID
ARG GROUP_ID

RUN apt-get update && apt-get install --no-install-recommends xz-utils git -y

RUN groupadd --system --gid ${GROUP_ID} jenkins
RUN useradd --system --gid jenkins --uid ${USER_ID} --shell /bin/bash --create-home jenkins
RUN mkdir -p /opt/cyn
RUN chown -R jenkins:jenkins /opt/cyn
RUN mkdir -p /home/jenkins/.gradle/
RUN echo "org.gradle.daemon=false" >| /home/jenkins/.gradle/gradle.properties
RUN chown -R jenkins:jenkins /home/jenkins/.gradle/

ENV GRADLE_USER_HOME /home/jenkins

USER jenkins

VOLUME /home/jenkins/cynergi-middleware

