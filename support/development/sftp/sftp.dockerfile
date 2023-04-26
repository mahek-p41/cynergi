FROM debian:bullseye-slim

ARG USER_ID=1001
ARG GROUP_ID=1001

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
apt-get upgrade -y && \
apt-get install openssh-server -y supervisor && \
mkdir -p /var/run/sshd && \
groupadd --system --gid ${GROUP_ID} sftpuser && \
useradd --system --create-home --gid ${GROUP_ID} sftpuser && \
echo "sftpuser:password" | chpasswd && \
sed -ri "s/#LogLevel INFO/LogLevel DEBUG/g" /etc/ssh/sshd_config

EXPOSE 22
VOLUME [ "/home/sftpuser" ]
# startup sshd in the forground and log to standard error
ENTRYPOINT ["/usr/sbin/sshd", "-D", "-e"]
