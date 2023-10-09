#!/usr/bin/env bash

[[ "$DEBUG_FLAG" == "true" ]] && set -x

BRANCH_NAME=${BRANCH_NAME#refs/heads/}
export BRANCH_NAME

VALID_BRANCH_NAME="true"

BRANCH_FOLDER=""
if [[ "$BRANCH_NAME" =~ .+/.+ ]]; then
  # everything before the last slash, plus the slash
  BRANCH_FOLDER=$(sed 's_/[^/]*$_/_' <<< "$BRANCH_NAME")
fi

if [[ ! "${BRANCH_NAME##*/}" =~ .+\..+ ]]; then
  echo "::warning:: The branch name does not contain a base branch name prefix." >&2
  VALID_BRANCH_NAME="false"

  BASE_BRANCH_NAME="${BRANCH_NAME}"
  # everything after the final slash
  BRANCH_CORE_NAME="${BRANCH_NAME##*/}"
else
  # everything between the last slash and the next dot
  BASE_BRANCH_NAME=$(sed "s_${BRANCH_FOLDER:- }__; s/\..*//" <<< "$BRANCH_NAME")
  # everything after the dot which follows the last slash
  BRANCH_CORE_NAME=$(sed "s_${BRANCH_FOLDER:- }${BASE_BRANCH_NAME}\.__" <<< "$BRANCH_NAME")
fi

export VALID_BRANCH_NAME
export BRANCH_FOLDER
export BASE_BRANCH_NAME
export BRANCH_CORE_NAME
