# Runs on cst machines   -  Look in cron.minute directory
#
logit() {
  echo "$(date) $*" >> $LOGGER
}
error() {
  ERR=$1
  shift 1
  logit "$*   ERROR: $ERR"
  exit $ERR
}
make_directory() {
  BASE=$(basename "$1")
  [ ! -d /tmp/ELIM ] && mkdir /tmp/ELIM
  [ ! -d /tmp/ELIM/$BASE ] && mkdir /tmp/ELIM/$BASE
}
install_Develop_system() {
  NOTIFY="develop $1"
  logit "Installing for Develop"
  install_this_system "/tmp/ELIM/D"
}
install_Staging_system() {
  NOTIFY="staging $1"
  logit "Installing for Staging"
  install_this_system "/tmp/ELIM/S"
}
install_this_system() {
  DIR="$1"
  make_directory $DIR
  mv $CLIENT $DIR
  mv $MIDDLE $DIR
  sh /opt/cyn/v01/SCRIPTS/ht.patch_elimination.sh
  RET=$?
  logit "Status: $RET ht.patch_elimination.sh"
  if [ $RET -eq 0 ]
  then
    #sh /opt/cyn/v01/SCRIPTS/ht.patch_FC430.sh
    logit "SKIPPING ht.patch_FC430.sh call"
    logit "Status: $? ht.patch_FC430.sh"
  else
    TEMP=/tmp/junk.a.$$
    tail -100 /opt/cyn/v01/cynmid/logs/cynergi-middleware.log > $TEMP
    sendEmail "Continuous build FAILED! for $NOTIFY"
    rm -f $TEMP
    exit 99
  fi
}
sendEmail() {
  sh /opt/cyn/v01/SCRIPTS/ht.cyn_email.sh -s "$*" -to montem@hightouchinc.com,garym@hightouchinc.com,vun@hightouchinc.com -fb $TEMP
}
LOGGER=/tmp/autoBuild2.log; chmod 666 $LOGGER
TRIGGER=/tmp/autoBuild.trigger

if [ -f $TRIGGER ]
then
  rm -f $TRIGGER
  cd /tmp
  CLIENT=$(ls -ltr cynergi-client*tar.xz | tail -1 | awk '{print $NF}')
  MIDDLE=$(ls -ltr cynergi-middleware*tar.xz | tail -1 | awk '{print $NF}')

  [ ! "$CLIENT" ] && error 1 "Missing cynergi-client tarball"
  [ ! "$MIDDLE" ] && error 2 "Missing cynergi-middleware tarball"

  IP=$(ifconfig | grep "inet addr" | head -1 | cut -d: -f2 | awk '{print $1}')

  unset NOTIFY
  case $IP in
    "172.29.3.143")
       install_Develop_system cst143
            ;;
    "172.29.3.145")
       install_Staging_system cst145
            ;;
    *)
     logit "Nothing to do for system $IP"
            ;;
  esac
  logit "Exit $0"
  if [ "$NOTIFY" ]
  then
    openssl genrsa -out /opt/cyn/v01/cynmid/jwt.pem 2048 1>/dev/null 2>/dev/null

    TEMP=/tmp/junk.a.$$
    initctl status cynergi-client > $TEMP
    initctl status cynergi-middleware >> $TEMP
    sendEmail "Continuous build finished for $NOTIFY"
    rm -f $TEMP
  fi
fi




