#!/usr/bin/env sh
# uses the pg_is_ready command documented https://www.postgresql.org/docs/9.3/app-pg-isready.html

MAX_TRIES=10
SLEEP_SECONDS=30

# possible exit status
# 0 success (server is accepting connections)
# 1 failure (server is rejecting connections)
# 2 failure (there was no response from the server)
# 3 failure (no attempt was made) I don't know really what this means docs says "for example due to invalid parameters"
pg_isready -q --host=cynergidb # TODO put this in a loop testing the exit status each time until success or a max tries is reached

exit $? # exit with value returned by pg_isready, should propigate out of the docker container (I think)
