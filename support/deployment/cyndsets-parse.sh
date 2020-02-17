#!/usr/bin/env bash

unset DSETS
for i in $(cat /opt/cyn/CYNDSETS | awk '{print $2 $3}')
do
  [ "$DSETS" ] && DSETS="${DSETS},$i" || DSETS="$i"
done
echo $DSETS
