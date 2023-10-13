#!/usr/bin/env bash
#script to help automate non-production deployment of updates to Project Elimination repos

logit() {
  echo "$(date) $*" >> $LOGGER
}
error() {
  ERR=$1
  shift 1
  logit "$*   ERROR: $ERR"
  exit "$ERR"
}
prepare_directories() {
  [[ ! -d /tmp/ELIM ]] && mkdir /tmp/ELIM
  [[ ! -d /tmp/ELIM/R ]] && mkdir /tmp/ELIM/R
  [[ -d /tmp/ELIM/S ]] && rm -vrf /tmp/ELIM/S
  [[ ! -L /tmp/ELIM/S ]] && ln -vs ./R /tmp/ELIM/S
  [[ -d /tmp/ELIM/D ]] && rm -vrf /tmp/ELIM/D
  [[ ! -L /tmp/ELIM/D ]] && ln -vs ./R /tmp/ELIM/D
}
install_this_system() {
  DIR="$1"
  prepare_directories
  mv "$TAR_CLIENT" "$DIR"
  mv "$TAR_MIDDLE" "$DIR"
  sh /opt/cyn/v01/SCRIPTS/ht.patch_elimination.sh
  RET=$?
  logit "Status: $RET ht.patch_elimination.sh"
  if [ $RET -eq 0 ]
  then
    #sh /opt/cyn/v01/SCRIPTS/ht.patch_FC430.sh
    logit "SKIPPING ht.patch_FC430.sh call"
    logit "Status: $? ht.patch_FC430.sh"
  else
    tail -100 /opt/cyn/v01/cynmid/logs/cynergi-middleware.log >> $MAIL_BODY
    sendEmail "Continuous build FAILED! for $NOTIFY"
    exit 99
  fi
}
sendEmail() {
  sh /opt/cyn/v01/SCRIPTS/ht.cyn_email.sh -s "$*" -to '7217b804.hightouchinc.com@amer.teams.ms' -fb $MAIL_BODY
}
LOGGER=/tmp/autoBuild2.log; chmod 666 $LOGGER
TRIGGER=/tmp/autoBuild.trigger
TAR_CLIENT="/tmp/cynergi-client-current.tar.xz"
TAR_MIDDLE="/tmp/cynergi-middleware-current.tar.xz"
JAR_MIDDLE="/opt/cyn/v01/cynmid/cynergi-middleware.jar"

if [ -f $TRIGGER ]
then
  rm -f $TRIGGER

  [[ ! -f "$TAR_CLIENT" ]] && error 1 "Missing cynergi-client tarball"
  [[ ! -f "$TAR_MIDDLE" ]] && error 2 "Missing cynergi-middleware tarball"

  MAIL_BODY=/tmp/junk.a.$$
  : > $MAIL_BODY #this just ensures we start with an empty file
  NOTIFY=$(hostname)
  logit "Installing for $NOTIFY"

  install_this_system "/tmp/ELIM/R"
  logit "Exit $0"
  if [ "$NOTIFY" ]
  then
    openssl genrsa -out /opt/cyn/v01/cynmid/jwt.pem 2048 1>/dev/null 2>/dev/null

    {
      initctl status cynergi-client
      cat /opt/cyn/v01/cynmid/buildlog || echo "WARNING: File '/opt/cyn/v01/cynmid/buildlog' not found"
      initctl status cynergi-middleware
      unzip -p "$JAR_MIDDLE" META-INF/MANIFEST.MF || echo "WARNING: Failed to extract 'META-INF/MANIFEST.MF' from '$JAR_MIDDLE'"
    } >> $MAIL_BODY
    sendEmail "Continuous build finished for $NOTIFY"
    rm -f $MAIL_BODY
  fi
fi
