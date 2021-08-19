#!/usr/bin/env bash
# uses the pg_isready command documented https://www.postgresql.org/docs/9.3/app-pg-isready.html

SLEEP_SECONDS=5

# 0 success (server is accepting connections)
# 1 failure (server is rejecting connections)
# 2 failure (there was no response from the server)
# 3 failure (no attempt was made) I don't know really what this means docs says "for example due to invalid parameters"
printf "%s" "Wait for $1 to become ready"
for count in {1..72}; do
  pg_isready --host=$1 --port=5432 > /dev/null 2> /dev/null
  if [ $? -eq 0 ]; then
    printf "\n%s" "$1 is accepting connections"
    exit 0
  elif [ $? -eq 3 ]; then
    printf "\n%s\n" "no attempt was made due to invalid parameters"
    exit 3
  else
    printf "%s" "."
    sleep $SLEEP_SECONDS
  fi
done

printf "\n%s\n" "$1 may not have started"
exit 1
