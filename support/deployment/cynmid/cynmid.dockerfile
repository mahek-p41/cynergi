ARG GROOVY_VER=3.0.9
FROM groovy:${GROOVY_VER}-jdk11 AS groovyImage

ARG GROOVY_VER=3.0.8
RUN mkdir -pv /tmp/gdk/${GROOVY_VER} && \
    cp -r /opt/groovy/* /tmp/gdk/${GROOVY_VER}

FROM adoptopenjdk:11.0.11_9-jdk-openj9-0.26.0-focal

ARG GROOVY_VER=3.0.8
ARG USER_ID=1001
ARG GROUP_ID=1001

RUN apt-get update && apt-get install --no-install-recommends xz-utils git -y

RUN groupadd --system --gid ${GROUP_ID} jenkins
RUN useradd --system --gid jenkins --uid ${USER_ID} --shell /bin/bash --create-home jenkins
RUN mkdir -p /opt/cyn/v01/cynmid/groovy/${GROOVY_VER}
COPY --from=groovyImage /tmp/gdk/${GROOVY_VER} /opt/cyn/v01/cynmid/groovy/${GROOVY_VER}
RUN chown -R jenkins:jenkins /opt/cyn
RUN mkdir -p /home/jenkins/.gradle/
RUN echo "org.gradle.daemon=false" >| /home/jenkins/.gradle/gradle.properties
RUN chown -R jenkins:jenkins /home/jenkins/.gradle/

ENV GRADLE_USER_HOME /home/jenkins
ENV GROOVY_VERSON $GROOVY_VER

USER jenkins

VOLUME /home/jenkins/cynergi-middleware
WORKDIR /home/jenkins/cynergi-middleware

