#!/usr/bin/env bash
#script to help automate non-production deployment of updates to Project Elimination repos

logit() {
  echo "$(date) : $WHO : $*" >> "$LOGGER"
}
error() {
  ERR=$1
  shift 1
  logit "$*   ERROR: $ERR"
  echo "$*"
  exit "$ERR"
}
checkForNewBuild() {
  cd "$DIR/$TYPE" || error 4 "Source path not found: $DIR/$WHO"

  if [ -f "$TRIGGER" ]
  then
    [[ ! -f $TAR_CLIENT || ! -f $TAR_MIDDLE ]] && error 2 "Missing one of the tarballs"
    sendThem
  fi
}
sendThem() {
  NEW_TRIGGER="autoBuild.trigger"

  logit "Found $TAR_CLIENT -- Sending to $WHO"
  logit "Found $TAR_MIDDLE -- Sending to $WHO"

  date > $NEW_TRIGGER
  {
    rsync -avxlz "./$TAR_CLIENT" "root@$WHO:/tmp"
    rsync -avxlz "./$TAR_MIDDLE" "root@$WHO:/tmp"
    rsync -avxlz "./$NEW_TRIGGER" "root@$WHO:/tmp"
  } >> "$LOGGER.status" 2>&1
  rm -f $TRIGGER $NEW_TRIGGER
}
##################################################################
set -x
#shellcheck disable=2143
[ ! "$(id | grep root)" ] && error 1 "User not root"

DIR=/home/jenkins/ELIMINATION
STAGE=STAGING
DEV=DEVELOP
# RELEASE=RELEASE
TAR_CLIENT="cynergi-client-current.tar.xz"
TAR_MIDDLE="cynergi-middleware-current.tar.xz"

TODAY=$(date +'%b %d')
T=$(echo "$TODAY" | tr " " "_")
LOGGER=/tmp/autoBuild.$T.log; chmod 666 "$LOGGER"

TRIGGER="build.trigger"

TARGET_PREFIX='autopatch_'
WHO=$(basename "$0")
logit "Checking..."

unset TYPE
[[ $WHO =~ ^"$TARGET_PREFIX".+ ]] || error 3 "This invoking symlink's name does not match the pattern '^autopatch_cst\d\d\d$'."
WHO=${WHO//$TARGET_PREFIX/}  #strips the prefix from the string
case $WHO in
  "develop")
    WHO=cst143
    TYPE="$DEV"
  ;;
  "staging")
    WHO=cst145
    TYPE="$STAGE"
  ;;
  *)
    TYPE=$WHO
esac

[ "$TYPE" ] && checkForNewBuild
