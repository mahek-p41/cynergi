logit() {
  echo "$(date) : $WHO : $*" >> $LOGGER
}
error() {
  ERR=$1
  shift 1
  logit "$*   ERROR: $ERR"
  echo "$*"
  exit $ERR
}
checkForNewBuild() {
  cd $DIR/$TYPE

  if [ -f "$TRIGGER" ]
  then
     [ -f $TAR_CLIENT ] && SENDC=$TAR_CLIENT
     [ -f $TAR_MIDDLE ] && SENDM=$TAR_MIDDLE
     #SENDC=$(ls -ltr ${TAR_CLIENT}* | tail -1 | awk '{print $NF}')
     #SENDM=$(ls -ltr ${TAR_MIDDLE}* | tail -1 | awk '{print $NF}')
     [ "$SENDC" -a "$SENDM" ] && sendThem || error 2 "Missing one of the tarballs"
  fi
}
sendThem() {
  NEW_TRIGGER="autoBuild.trigger"

  logit "Found $SENDC -- Sending to $WHO"
  logit "Found $SENDM -- Sending to $WHO"

  date > $NEW_TRIGGER
  rsync -avxlz --delete-during ./$SENDC root@$WHO:/tmp >> $LOGGER.status 2>&1
  rsync -avxlz --delete-during ./$SENDM root@$WHO:/tmp >> $LOGGER.status 2>&1
  rsync -avxlz --delete-during ./$NEW_TRIGGER root@$WHO:/tmp >> $LOGGER.status 2>&1
  rm -f $TRIGGER $NEW_TRIGGER
}
##################################################################
[ ! "$(id | grep root)" ] && error 1 "User not root"

DIR=/home/jenkins/ELIMINATION
STAGE=STAGING
DEV=DEVELOP
RELEASE=RELEASE
TAR_CLIENT="cynergi-client-current.tar.xz"
TAR_MIDDLE="cynergi-middleware-current.tar.xz"

TODAY=$(date +'%b %d')
T=$(echo $TODAY | tr " " "_")
LOGGER=/tmp/autoBuild.$T.log; chmod 666 $LOGGER

TRIGGER="build.trigger"

WHO=$(basename $0)
logit "Checking..."

unset TYPE
case $WHO in
   "cst143")
      TYPE="$DEV"
	;;
   "cst145")
      TYPE="$STAGE"
	;;
   *)
      error 5 "Cant call this script directly"
esac

[ "$TYPE" ] && checkForNewBuild

