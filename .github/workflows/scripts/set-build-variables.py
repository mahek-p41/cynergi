#!/usr/bin/env python3

import os
import json
import uuid
from typing import Dict

# A list of key-value pairs separated by newlines
outVars = ""

# Only use the name after the last / in the ref
branchName = os.environ["GITHUB_REF_NAME"].rsplit("/", 1)[-1]
outVars += f"\nbranchName={branchName}"

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
  outVars += f"\n{key}={value}"

# Read releaseVersion from gradle.properties
with open("gradle.properties", "r") as gpFile:
  lines = gpFile.readlines()
  releaseVersion = ""
  for line in lines:
    if line.startswith("releaseVersion"):
      releaseVersion = line.split("=")[1].strip()
      break
if releaseVersion == "":
  raise ValueError(f"Required key '{releaseVersion}' is missing in file gradle.properties")
outVars += f"\nreleaseVersion={releaseVersion}"

networkId = str(uuid.uuid4())
outVars += f"\nnetworkId={networkId}"

jenkinsUid = str(os.getuid())
outVars += f"\njenkinsUid={jenkinsUid}"

jenkinsGid = str(os.getgid())
outVars += f"\njenkinsGid={jenkinsGid}"

# Output the results
output_file_path = os.environ["GITHUB_OUTPUT"]
with open(output_file_path, "a") as output_file:
  output_file.write(outVars)
