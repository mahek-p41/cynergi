# Project E Automated Patching Scripts

## Summary

The `autopatch_send.sh` and `autopatch_receive.sh` scripts are used to allow continuous deployment of Project E updates. It is only intended for use within internal CST virtual machines. These are not automatically configured to run anywhere; any intended use must be manually configured. The `autopatch_send.sh` script resides on the Gold Box, while the `autopatch_receive.sh` script resides on any server intended to receive these automated updates.

## Setup

### `autopatch_send.sh`

Create a symlink to this script in a cron directory with the name of the CST machine which should receive this deployment:
```bash
ln -vs /home/jenkins/SCRIPTS/autopatch_send.sh /etc/cron.qhourly/autopatch_cst144
# '/etc/cron.qhourly/autopatch_cst144' -> '/home/jenkins/SCRIPTS/autopatch_send.sh'
```
**The name is important**: "autopatch_" prefixing a target. The above example will cause the script to target `cst144` for auto-patching. All CST machines which have been configured and are recognized by the Gold Box (i.e. address is in the `/etc/hosts` file) are valid targets. `autopatch_develop` targets `cst143` and `autopatch_staging` targets `cst145`; these special targets must be used if the deploy script is uploading the tarballs to the `DEVELOP` and `STAGING` directories, respectively, under the `/home/jenkins/ELIMINATION/` path. Otherwise, the other targets will look for tarballs under a self-named directory under the `/home/jenkins/ELIMINATION/` path (e.g. `/home/jenkins/ELIMINATION/cst144/`).

### `autopatch_receive.sh`

This file *is not included* in Project E tarballs. On any CST machine which should be receiving automated patching, manually upload this script (such as at `/opt/cyn/v01/cynmid/autopatch_receive.sh`). To enable processing of sent patches, create a symlink to this script in a cron directory:
```bash
ln -vs /opt/cyn/v01/cynmid/autopatch_receive.sh /etc/cron.minute/t100_continuous_build.sh
# '/etc/cron.minute/t100_continuous_build.sh' -> '/opt/cyn/v01/cynmid/autopatch_receive.sh'
```

## Usage

1. Upload newly-built tarballs for both `client` and `middleware` to the Gold Box/rsssDev
    - `cynergi-client-current.tar.xz` and `cynergi-middleware-current.tar.xz` respectively
    - under a subdirectory of the `/home/jenkins/ELIMINATION/` path
    - name the directory to match the targeted CST machine
        - e.g. `/home/jenkins/ELIMINATION/cst144/`
2. Create a file in that same directory named `build.trigger`
    - `touch /home/jenkins/ELIMINATION/cst144/build.trigger`
3. The `autopatch_send.sh` cronjob will detect the change (and trigger) and send the patch to the targeted machine
4. The `autopatch_receive.sh` cronjob on the targeted machine will detect the patch and install it
    - this overwrites whatever version of Project E is currently installed
    - this includes stopping and restarting all of the Project E services to fully apply the update
