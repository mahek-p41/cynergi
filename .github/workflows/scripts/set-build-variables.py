#!/usr/bin/env python3

import os
import json
import uuid
from typing import Dict

def debugLog(message: str):
  print(f"::debug:: {message}")

# A list of key=value pairs to send to GITHUB_OUTPUT
outVars = []

# Read the DEBUG_FLAG environment variable and set debugMode accordingly
debugFlag = os.environ.get("DEBUG_FLAG", "false")
debugMode = "true" if debugFlag == "true" else "false"
outVars.append(f"debugMode={debugMode}")
debugLog(outVars[-1])

# Only use the name after the last / in the ref
branchName = os.environ["GITHUB_REF_NAME"].rsplit("/", 1)[-1]
outVars.append(f"branchName={branchName}")
debugLog(outVars[-1])

#expecting something like this: {"default":{"micronautEnv":"","releaseEnvironment":"","deployTarget":[]},"master":{"micronautEnv":"prod","releaseEnvironment":"RELEASE","deployTarget":[]},"staging":{"micronautEnv":"cstdevelop","releaseEnvironment":"STAGING","deployTarget":["cst145"]},"develop":{"micronautEnv":"cstdevelop","releaseEnvironment":"DEVELOP","deployTarget":["cst143","cst144"]}}
jsonInput = os.environ["SOURCE_TARGET_MAP"]
sourceTargetMap: Dict[str, Dict[str, str]] = json.loads(jsonInput)

#defaulting to "develop" if the current branch is not defined in the JSON
branchData = sourceTargetMap.get(branchName, sourceTargetMap.get("default", {}))

#validate JSON structure
requiredKeys = ["micronautEnv", "releaseEnvironment", "deployTargets"]
for key in requiredKeys:
  if key not in branchData:
    raise ValueError(f"Required key '{key}' is missing in '{branchName}' branchData")

for key, value in branchData.items():
  if isinstance(value, list):
    value = " ".join(value) # Join array elements with spaces
  outVars.append(f"{key}={value}")
  debugLog(outVars[-1])

# Read releaseVersion from gradle.properties
with open("gradle.properties", "r") as gpFile:
  lines = gpFile.readlines()
  debugLog(f"gradle.properties\n{lines}")
  releaseVersion = ""
  for line in lines:
    if line.startswith("releaseVersion"):
      releaseVersion = line.split("=")[1].strip()
      break
if releaseVersion == "":
  raise ValueError(f"Required key '{releaseVersion}' is missing in file gradle.properties")
outVars.append(f"releaseVersion={releaseVersion}")
debugLog(outVars[-1])

networkId = str(uuid.uuid4())
outVars.append(f"networkId={networkId}")
debugLog(outVars[-1])

jenkinsUid = str(os.getuid())
outVars.append(f"jenkinsUid={jenkinsUid}")
debugLog(outVars[-1])

jenkinsGid = str(os.getgid())
outVars.append(f"jenkinsGid={jenkinsGid}")
debugLog(outVars[-1])

# Check for a fast-fail flag either from the environment variable or from the latest commit message
fastfail = os.environ.get("FASTFAIL_FLAG", "false")
if fastfail != "true":
  commitMessage = os.environ["COMMIT_MESSAGE"]
  fastfail = "true" if "[fastfail]" in commitMessage else "false"
outVars.append(f"fastfail={fastfail}")
debugLog(outVars[-1])

# Output the results
outputFilePath = os.environ["GITHUB_OUTPUT"]
with open(outputFilePath, "a") as outputFile:
  outputFile.write("\n".join(outVars))
