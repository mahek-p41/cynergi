#!/usr/bin/env bash

[[ "$DEBUG_FLAG" == "true" ]] && set -x

# grab the whole list of branches; strip out any leading spaces and asterisks
mapfile -t baseBranchesArray < <(git branch --list | sed 's/^\*\? *//')

# find the closest ancestor (comparing commit distances) of the current branch from the list of local base branches
POSSIBLE_BASE_BRANCH_NAME=develop
shortestDistance=999999999
for branch in "${baseBranchesArray[@]}"; do
  [[ "$BRANCH_NAME" == "$branch" ]] && continue
  baseBranchSha=$(git rev-list -n 1 "$branch") || continue
  baseBranchShaDistance=$(git rev-list --count "$baseBranchSha..HEAD")

  if [[ $baseBranchShaDistance -lt $shortestDistance ]]; then
    shortestDistance=$baseBranchShaDistance
    POSSIBLE_BASE_BRANCH_NAME=$branch
  fi
done

export POSSIBLE_BASE_BRANCH_NAME