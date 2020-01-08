#!/usr/bin/env bash
## description: starts the cynergidb, cynergidevelopdb and the fastinfo_production databases and makes them available on localhost:6432 and returns once they are up and running

./commands/db/stop/test.sh
./commands/db/start/test.sh
