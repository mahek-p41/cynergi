#!/usr/bin/env bash

#Wrapper script for rsync backup. To be used on a system before major, possibly breaking upgrades
#(such as to new HOA). This is an alternative to VM system snapshots, which are better in almost
#every way, but only on systems hosted by HTI.

sourceDirs=(/Z /opt)
destinationDir=/bak
logDir=/bak/logs
logFile=rsync-backup.log
dryRun=true

PROGRAM=${0##*/}
PID=$$

#taken from https://stackoverflow.com/a/76512982/4808707 on 2023-11-15
function decolor() {
  echo -e "$*" | sed -E "s/\x1B\[([0-9]{1,2}(;[0-9]{1,2})*)?[m,K,H,f,J]//gm" | sed -E "s/\x1B\([A-Z]{1}(\x1B\[[m,K,H,f,J])?//gm"
}

function logit() {
  local plainMessage
  plainMessage=$(decolor "$*")
  if [[ "-s" == "${1:-}" ]]; then
    shift 1
    #remove the -s from the plain message
    plainMessage=${plainMessage:3}
    logger --priority local0.notice --id="${PID}" --tag "${PROGRAM}" "${plainMessage}"
  fi
  echo -e "$*"
  [[ -w "${logDir}/${logFile}" ]] && printf '%(%Y-%m-%dT%H:%M:%S%z)T %s\n' "-1" "${plainMessage}" >> "${logDir}/${logFile}"
}

function spacer() {
  logit ""
  logit "==============================="
  logit ""
}

function showUsage() {
  echo "Usage: ${PROGRAM} [-f] [-s sourceDir] [-d destinationDir] [-l logDir] [-h]"
  echo "Options:"
  echo "  -f: force"
  echo "  -s: source directory"
  echo "  -d: destination directory"
  echo "  -l: log directory"
  echo "  -h: help"
}

#override the exit call
function _exit() {
  local exitCode
  if [[ "${1:-}" =~ [0-9]+ ]]; then
    exitCode=$1
    shift 1
  else
    exitCode=0
  fi
  logit -s "$*"
  spacer
  builtin exit "${exitCode}"
}

caller=${SUDO_USER:-}
spacer
logit -s "Starting rsync backup..."
logit -s "User: ${caller}"
logit -s "Command: $0 $*"

if [[ "${EUID}" -ne 0 ]]; then
  _exit 1 "This script must be run as a sudoer. Exiting..."
fi
if [[ -z ${caller} ]]; then
  _exit 1 "This script must be run as a sudoer. Exiting..."
fi

while getopts ":fs:d:l:h" opt; do
  case ${opt} in
    f)
      dryRun=false
      ;;
    s)
      sourceDirs=("${OPTARG}")
      if [[ "${sourceDirs[0]}" =~ /$ ]]; then
        logit "\e[31mWARNING: source directory '${sourceDirs[0]}' ends with a slash. \e[0mThis will cause the contents of '${sourceDirs[0]}' to be"
        logit "copied into '${destinationDir}' instead of '${sourceDirs[0]%%/}' itself. If this is not intended, restart this script."
        sleep 5
      fi
      ;;
    d)
      destinationDir="${OPTARG}"
      ;;
    l)
      logDir="${OPTARG}"
      ;;
    h)
      showUsage
      _exit 0
      ;;
    *)
      showUsage
      _exit 1 "Invalid option: -${OPTARG}"
      ;;
  esac
done

if [[ ! -d "${destinationDir}" ]]; then
  logit -s "Destination directory ${destinationDir} does not exist. Creating..."
  mkdir -p "${destinationDir}"
fi

for dir in "${sourceDirs[@]}"; do
  if [[ ! -d "${dir}" ]]; then
    _exit 1 "Source directory ${dir} does not exist. Exiting..."
  fi
done

if [[ ! -d "${logDir}" ]]; then
  logit -s "Log directory ${logDir} does not exist. Creating..."
  mkdir -p "${logDir}"
fi

if [[ ! -f "${logDir}/${logFile}" ]]; then
  logit -s "Log file ${logDir}/${logFile} does not exist. Creating..."
  touch "${logDir}/${logFile}"
  _exit 1 "Please rerun this command now that the log file has been created."
fi

if [[ ! -w "${logDir}/${logFile}" ]]; then
  _exit 1 "Log file ${logDir}/${logFile} is not writeable. Exiting..."
fi

overwriteFileCount=0
overwriteDirCount=0

for dir in "${sourceDirs[@]}"; do
  rsyncArgs=(-aiHAXP --stats --dry-run "${dir}" "${destinationDir}")
  logit "# rsync ${rsyncArgs[*]}"
  rsyncResults=$(nice -n 19 ionice -c3 rsync ${rsyncArgs[*]})
  logit "${rsyncResults}"

  while read -r line; do
    if [[ "${line}" =~ ^Number\ of\ (deleted\ files|(regular)?\ files\ transferred):\ ([0-9]+)(\ dir:\ ([0-9]+))?$ ]]; then
      overwriteFileCount=$((overwriteFileCount + BASH_REMATCH[3]))
      overwriteDirCount=$((overwriteDirCount + BASH_REMATCH[5]))
    fi
  done <<< "${rsyncResults}"

  spacer
done

logit -s "Overwrite warnings:"
if [[ ${overwriteFileCount} -gt 0 ]]; then
  logit -s "  \e[33;1m\e[5m${overwriteFileCount} files under the ${destinationDir} directory will be overwritten if this backup is continued.\e[0m"
fi
if [[ ${overwriteDirCount} -gt 0 ]]; then
  logit -s "  \e[33;1m\e[5m${overwriteDirCount} directories under the ${destinationDir} directory will be overwritten if this backup is continued.\e[0m"
fi
[[ $((overwriteFileCount + overwriteDirCount)) -eq 0 ]] && logit -s "  None."

spacer

logit "For help interpreting the output of the rsync command, read the '-i, --itemize-changes' section at:"
logit '  https://linux.die.net/man/1/rsync#:~:text=blocking%20I/O.)-,%2Di%2C%20%2D%2Ditemize%2Dchanges,The%20x%20means%20that%20the%20extended%20attribute%20information%20changed.,-One%20other%20output'
sleep 5

if [[ "${dryRun}" == true ]]; then
  _exit 0 "Dry run complete. Exiting..."
fi

logit "If you understand the consequences of continuing this command and wish to proceed, type 'yes'"
logit "and press enter. Otherwise, type 'no' and press enter."
logit "Continue? [y/N] "
read -r response
logit ""
logit "Response: ${response}"
if [[ ! "${response}" =~ ^([yY][eE][sS]|[yY])$ ]]; then
  _exit 1 "Exiting without completing the rsync run..."
fi

spacer

for dir in "${sourceDirs[@]}"; do
  rsyncArgs=(-aiHAXP --stats "${dir}" "${destinationDir}")
  logit -s "# rsync ${rsyncArgs[*]}"

  nice -n 19 ionice -c3 rsync ${rsyncArgs[*]}
done

_exit 0 "Successfully completed rsync backup from '${sourceDirs[*]}' into '${destinationDir}."
