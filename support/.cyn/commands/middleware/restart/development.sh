#!/usr/bin/env bash
## description: starts the cynergidb and the fastinfo_production databases and makes them available on localhost:6432 and returns once they are up and running

./commands/middleware/stop/development.sh
./commands/middleware/start/development.sh
