#!/usr/bin/env bash

export JAVA_HOME=/opt/cyn/v01/cynmid/java/openj9/@@JAVA_VER_BUILD@@/
export GROOVY_HOME=/opt/cyn/v01/cynmid/groovy/@@GROOVY_VER@@/

$GROOVY_HOME/bin/groovy $@
