postgresCheck() {
# 0 = Accepting connections
# 1 = Rejecting connections
# 2 = No response
# 3 = Invalid parameters
  /usr/pgsql-9.3/bin/pg_isready $IS_READY_PARMS   2>/dev/null
  STATUS_RET=$?
  return $STATUS_RET
}
postgresUp() {
  exit 0
}
postgresDown() {
  WHO="solutionsdelivery@hightouchinc.com,montem@hightouchinc.com"
  SUBJECT="Postgres is down Error: $STATUS_RET"
  MESSAGE="Error code $STATUS_RET is"

  case $STATUS_RET in
	1)	ERROR="$MESSAGE 'Rejecting connections'"			;;
	2)	ERROR="$MESSAGE 'No response'"					;;
	3)	ERROR="$MESSAGE 'Invalid parameters which are $IS_READY_PARMS"	;;
	*)	ERROR="$MESSAGE 'Unknown!'"					;;
  esac

  sh $HT_CYN_SCRIPTS/ht.cyn_email.sh -s "$SUBJECT" -to "$WHO" -b "$ERROR" &
  exit 1
}
####################################################
source /opt/cyn/ht.cyn_base_vars.sh
IS_READY_PARMS="--username=postgres --quiet"

[ $1 ] && MAX=$1 || MAX=150    # Wait 5 minutes before sending emails
[ $2 ] && SLEEP=$2 || SLEEP=2


ARE_WE_UP=false
for i in $(seq 1 $MAX)
do
   postgresCheck
   [ $? -eq 0 ] && ARE_WE_UP=true && break
   sleep $SLEEP
done

[ $ARE_WE_UP = true ] && postgresUp || postgresDown
