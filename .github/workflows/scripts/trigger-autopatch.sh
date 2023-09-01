#!/usr/bin/env bash
#shellcheck disable=2086

${DEBUG_MODE}
set -o errexit -o pipefail -o noclobber -o nounset

cd $DEPLOY_ROOT_PATH

for target in $DEPLOY_TARGETS; do
  if [[ ! -d $target ]]; then
    mkdir $target
    chmod a+w $target
  fi

  cd $target
  find ../${RELEASE_ENVIRONMENT} -type f -name '*current*' -exec ln -svf {} ./ \;

  touch ${TRIGGER}
  cd -
done
