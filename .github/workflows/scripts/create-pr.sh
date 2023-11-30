#!/usr/bin/env bash

#uses the gh cli to check if a PR already exists for merging the `auto-merge-staging-to-develop` branch into `develop`

set -euo pipefail

PROGRAM="${0##*/}"

function usage() {
  cat <<-EoH #the dash allows the here-doc to ignore leading tab characters
		Usage: ${PROGRAM} -R <repository> -B <base-branch> -H <head-branch>
      -t <title> -b <body> [-h] [-d <dry-run>] [-s <sync-branch>]
      [-l <label> [-l <label> ...]]
      [-r <reviewer-username> [-r <reviewer-username> ...]]

    Options:
		  -R, --repo      repository (required)
		  -B, --base      base branch (required)
		  -H, --head      head branch (required)
		  -t, --title     title (required)
		  -b, --body      body of the PR's description (required)
		  -h. --help      display this help message
		  -d, --dry-run   dry run (don't actually create the PR)
		                  ex. 'hightouchinc/cynergi-middleware'
		  -s, --sync      sync branch to use in place of the head branch for rebasing
		  -l, --label     label (repeat for multiple labels)
		  -r, --reviewer  reviewer username (repeat for multiple reviewers)
		EoH
}

DRY_RUN=0
DRY_RUN_SAFETY=
REPO=
BASE_BRANCH=
HEAD_BRANCH=
TITLE=
BODY=
SYNC_BRANCH=

while getopts ":B:b:dhH:l:R:r:s:t:-:" opt; do
  case ${opt} in
    B ) BASE_BRANCH=${OPTARG} ;;
    b ) BODY=${OPTARG} ;;
    d )
        unset DRY_RUN
        declare -r DRY_RUN=1
        unset DRY_RUN_SAFETY
        declare -r DRY_RUN_SAFETY="echo ::notice:: Dry run, skipping command: "
    ;;
    H ) HEAD_BRANCH=${OPTARG} ;;
    h ) usage; exit 0;;
    l ) LABELS+=("${OPTARG}") ;;
    R ) REPO=${OPTARG} ;;
    r ) REVIEWERS+=("${OPTARG}") ;;
    s ) SYNC_BRANCH=${OPTARG} ;;
    t ) TITLE=${OPTARG} ;;
    - )
      case ${OPTARG} in
        base=*      ) BASE_BRANCH=${OPTARG#*=} ;;
        base        )
                      BASE_BRANCH="${!OPTIND}"
                      ((OPTIND++))
        ;;
        body=*      ) BODY=${OPTARG#*=} ;;
        body        )
                      BODY="${!OPTIND}"
                      ((OPTIND++))
        ;;
        dry-run     )
                      unset DRY_RUN
                      declare -r DRY_RUN=1
                      unset DRY_RUN_SAFETY
                      declare -r DRY_RUN_SAFETY="echo ::notice:: Dry run, skipping command:"
        ;;
        head=*      ) HEAD_BRANCH=${OPTARG#*=} ;;
        head        )
                      HEAD_BRANCH="${!OPTIND}"
                      ((OPTIND++))
        ;;
        help        ) usage; exit 0;;
        label=*     ) LABELS+=("${OPTARG#*=}") ;;
        label       )
                      LABELS+=("${!OPTIND}")
                      ((OPTIND++))
        ;;
        repo=*      ) REPO=${OPTARG#*=} ;;
        repo        )
                      REPO="${!OPTIND}"
                      ((OPTIND++))
        ;;
        reviewer=*  ) REVIEWERS+=("${OPTARG#*=}") ;;
        reviewer    )
                      REVIEWERS+=("${!OPTIND}")
                      ((OPTIND++))
        ;;
        sync-branch=* ) SYNC_BRANCH=${OPTARG#*=} ;;
        sync-branch   )
                      SYNC_BRANCH="${!OPTIND}"
                      ((OPTIND++))
        ;;
        title=*     ) TITLE=${OPTARG#*=} ;;
        title       )
                      TITLE="${!OPTIND}"
                      ((OPTIND++))
        ;;
        *           ) echo "Invalid option: --${OPTARG}" >&2; usage; exit 1;;
      esac
    ;;
    * ) echo "Invalid option: -${OPTARG}" >&2; usage; exit 1;;
  esac
done

#ensure required parameters are set
if [[ -z "${BASE_BRANCH}" ]]; then
  echo "::error:: Missing required parameter: -B, --base"
  usage
  exit 1
fi
if [[ -z "${BODY}" ]]; then
  echo "::error:: Missing required parameter: -b, --body"
  usage
  exit 1
fi
if [[ -z "${HEAD_BRANCH}" ]]; then
  echo "::error:: Missing required parameter: -H, --head"
  usage
  exit 1
fi
if [[ -z "${REPO}" ]]; then
  echo "::error:: Missing required parameter: -R, --repo"
  usage
  exit 1
fi
if [[ -z "${TITLE}" ]]; then
  echo "::error:: Missing required parameter: -t, --title"
  usage
  exit 1
fi
if [[ -z "${SYNC_BRANCH}" ]]; then
  SYNC_BRANCH="sync/merge-${HEAD_BRANCH}-to-${BASE_BRANCH}"
fi

commits=$(git log "origin/${HEAD_BRANCH}" "^origin/${BASE_BRANCH}")
if [[ -z "${commits}" ]]; then
  echo "::notice:: No new commits to merge into '${BASE_BRANCH}' from '${HEAD_BRANCH}'"
  exit 0
fi

git switch "${HEAD_BRANCH}"
baseSha=$(git rev-parse "origin/${BASE_BRANCH}")
headBase=$(git merge-base "HEAD" "origin/${BASE_BRANCH}")
if [[ "${baseSha}" != "${headBase}" ]] && ! git merge --no-edit "origin/${BASE_BRANCH}"; then
  echo "::error:: Unable to merge '${HEAD_BRANCH}' into '${BASE_BRANCH}'. Check the PR for a merge conflict."
  git merge --abort || : #okay to fail
fi

#if the sync branch doesn't already exist, create it
if ! git switch "${SYNC_BRANCH}" 2>/dev/null; then
  echo "::notice:: Sync branch '${SYNC_BRANCH}' does not exist. Creating it."
  git switch --create "${SYNC_BRANCH}"
else
  syncBase=$(git merge-base "${SYNC_BRANCH}" "origin/${BASE_BRANCH}")
  if [[ "${baseSha}" != "${syncBase}" ]]; then
    echo "::warning:: PR base branch needs to be updated. Merging ..."
    #TODO should this instead just reset the sync branch to the head branch?
    if ! git merge --no-edit "origin/${BASE_BRANCH}"; then
      echo "::error:: Unable to merge '${SYNC_BRANCH}' into '${BASE_BRANCH}'. Check the PR for a merge conflict."
      git merge --abort || : #okay to fail
    fi
  fi

  #make sure the sync branch is up to date with the head branch
  diffstat=$(git diff --stat "${HEAD_BRANCH}..${SYNC_BRANCH}")
  if [[ -n "${diffstat}" ]]; then
    echo "::notice:: Syncing '${SYNC_BRANCH}' with '${HEAD_BRANCH}'"

    if ! git merge --ff-only "${HEAD_BRANCH}"; then
      echo "::warning:: '${HEAD_BRANCH}' history has diverged from '${SYNC_BRANCH}'. Resetting..."
      git reset --hard "${HEAD_BRANCH}"
    fi
  fi
fi

#if the local sync branch does not match the remote sync branch, push an update
if ! git diff --quiet "origin/${SYNC_BRANCH}" "${SYNC_BRANCH}" 2>/dev/null; then
  echo "::warning:: Sync branch '${SYNC_BRANCH}' does not match remote. Updating the remote."
  ${DRY_RUN_SAFETY} git push --force-with-lease origin "${SYNC_BRANCH}"
fi

#check if a matching PR already exists
jsonOptions="number"
pr=$(gh pr list --base "${BASE_BRANCH}" --head "${SYNC_BRANCH}" --repo "${REPO}" --json "${jsonOptions}")
prNumber=$(jq -r '.[0].number' <<<"${pr}" 2>/dev/null || :)

#if a matching PR already exists, check if it needs to be rebased
if [[ "${prNumber}" != "null" ]]; then
  echo "::notice:: PR already exists: ${prNumber}"
  exit 0
fi

if [[ "${prNumber}" == "null" ]]; then
  echo "::warning:: A PR to merge '${HEAD_BRANCH}' into '${BASE_BRANCH}' does not exist. Creating one now."

  # Start building the command string
  cmd="gh pr create \
      --base '${BASE_BRANCH}' \
      --body '${BODY}' \
      --head '${SYNC_BRANCH}' \
      --repo '${REPO}' \
      --title '${TITLE}'"

  # Iterate over the LABELS array
  for label in "${LABELS[@]}"; do
    # Append each element to the command string with the prefix "--label "
    cmd+=" --label '${label}'"
  done

  # Iterate over the REVIEWERS array
  for reviewer in "${REVIEWERS[@]}"; do
    # Append each element to the command string with the prefix "--reviewer "
    cmd+=" --reviewer '${reviewer}'"
  done

  # Execute the command string
  eval "${cmd}"
fi

if [[ "${DRY_RUN}" != 0 ]]; then
  echo "::notice:: Dry run complete"
  exit 1
fi

exit 0
