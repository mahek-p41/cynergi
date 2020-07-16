getFile() {
  FILE=$(ls ${1}__* 2>/dev/null)
    [ "$FILE" ] && validate "$FILE"
}
validate() {
  ONE=$(getVersion "$1")
  TWO=$(getSQL "$1")
  [ ! -f "${ONE}__${TWO}" ] && echo && echo "ERROR: Invalid file name for: $1" && exit 90 || return 0
}

renumber() {
  ONE=$(getVersion "$1")
  TWO=$(getSQL "$1")

  NUMBER=$(echo "$ONE" | cut -c2-)
  BUMP=$(expr $NUMBER + 1)
  NEW_FILE="V${BUMP}__${TWO}"
  echo "mv $1 $NEW_FILE" | tee -a $TEMPSH
  START="V${BUMP}"
}

getVersion() {
  echo "$1" | cut -d'_' -f1
}
getSQL() {
  echo "$1" | cut -d'_' -f3
}

doit() {
  while :
  do
    getFile "$START" : $FILE is set
    [ $? -ne 0 ] && return 0
    renumber "$FILE"
  done
}

 ##############################################################################################
cd ../../src/main/resources/db/migration/postgres
[ ! "$1" ] && echo -n "Start renumbering with which one?: " && read RENUM || RENUM="$1"
TEMPSH=/tmp/temp_shell.$$.sh; rm -f $TEMPSH

START=$(getVersion "$RENUM")
[ $(echo $START | cut -c1) != "V" ] && START="V${START}"

KEEP="$START"


doit $KEEP
if [ -s $TEMPSH ]
then
  echo -n "Does this look okay? [Y/N]: "; read ANS
  ANS=$(echo "$ANS" | tr '[:lower:]' '[:upper:]')
  [ "$ANS" = "Y" ] && sh $TEMPSH
else
  echo "Didn't find anything"
fi

