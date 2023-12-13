#!/usr/bin/env python3

import sys
import argparse
import uuid

#prepends messages with "::debug:: " and outputs to stderr
def debugLog(message: str):
  print(f"::debug::{message}", file=sys.stderr)

parser = argparse.ArgumentParser(description="Parse inputs from GitHub Actions")
parser.add_argument("--baseBranch", required=True, help="The branch to merge into")
parser.add_argument("--body", required=True, help="The body of the PR")
parser.add_argument("--committerName", required=True, help="The name of the committer")
parser.add_argument("--committerEmail", required=True, help="The email address of the committer")
parser.add_argument("--debug", required=False, help="Whether to produce debug logs")
parser.add_argument("--dryRun", required=False, help="Whether to run in dry-run mode")
parser.add_argument("--headBranch", required=True, help="The branch to merge from")
parser.add_argument("--labels", required=False, help="A space-separated list of labels to apply to the PR")
parser.add_argument("--repository", required=True, help="The repository to create the PR in")
parser.add_argument("--reviewers", required=False, help="A space-separated list of reviewers to add to the PR")
parser.add_argument("--syncBranch", required=False, help="The temporary branch, cloned from the head branch, to use for the actual merge")
parser.add_argument("--title", required=True, help="The title of the PR")
args = parser.parse_args()

# A list of key=value pairs to output
outVars = []

# Process each input differently as needed

debugFlag = "true" if args.debug == "true" else "false"
outVars.append(f"DEBUG_FLAG={debugFlag}")
debugLog(outVars[-1])

baseBranch = args.baseBranch
outVars.append(f"BASE_BRANCH={baseBranch}")
debugLog(outVars[-1])

randomDelimiter = str(uuid.uuid4())
body = f"""BODY<<{randomDelimiter}
{args.body}
{randomDelimiter}"""
outVars.append(f"{body}")
debugLog(outVars[-1])

committerName = args.committerName
outVars.append(f"COMMITTER_NAME={committerName}")
debugLog(outVars[-1])

committerEmail = args.committerEmail
outVars.append(f"COMMITTER_EMAIL={committerEmail}")
debugLog(outVars[-1])

dryRunOption = "--dry-run" if args.dryRun == "true" else ""
outVars.append(f"DRY_RUN_OPTION={dryRunOption}")
debugLog(outVars[-1])

headBranch = args.headBranch
outVars.append(f"HEAD_BRANCH={headBranch}")
debugLog(outVars[-1])

labelsOption = " ".join([f"--label {label}" for label in args.labels.split()])
outVars.append(f"LABELS_OPTION={labelsOption}")
debugLog(outVars[-1])

repository = args.repository
outVars.append(f"REPOSITORY={repository}")
debugLog(outVars[-1])

reviewersOption = " ".join([f"--reviewer {reviewer}" for reviewer in args.reviewers.split()])
outVars.append(f"REVIEWERS_OPTION={reviewersOption}")
debugLog(outVars[-1])

syncOption = f"--sync {args.syncBranch}" if args.syncBranch else ""
outVars.append(f"SYNC_OPTION={syncOption}")
debugLog(outVars[-1])

title = args.title
outVars.append(f"TITLE={title}")
debugLog(outVars[-1])

# Output the results to stdout
print("\n".join(outVars))
