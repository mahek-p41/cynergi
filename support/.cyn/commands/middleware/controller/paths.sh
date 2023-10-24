#!/usr/bin/env bash

pushd ../../ > /dev/null

find ./src/main/kotlin -name "*Controller.kt" -exec cat {} \; | grep @Controller | cut -d'"' -f2 | grep -v @Controller | sort
