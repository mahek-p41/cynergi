# Notes About Tarball Deploy

The workflow expects the following repo secrets and variables to be set.

Secrets:
  - `GOLDBOX_IP_ADDRESS`
  - `CYNERGI_DEPLOY_JENKINS_USR`
  - `CYNERGI_DEPLOY_JENKINS_PSW`

Variables:
  - `GOLDBOX_SSH_OPTIONS`
    - A string of options needed for an ssh command to be able to successfully connect to the Gold Box. If that ever gets upgraded, this will need to be updated. It _may_ need to be updated if our self-hosted runner is ever upgraded.
    - Current value: `-oStrictHostKeyChecking=no -oHostKeyAlgorithms=+ssh-rsa -oKexAlgorithms=+diffie-hellman-group-exchange-sha1`
  - `SOURCE_TARGET_MAP`
    - A JSON object string with objects keyed to match branch names, plus a `default` object. Each branch's object should have the keys `micronautEnv` and `releaseEnvironment`, each with string values, and `deployTargets` which is an array of 0 or more strings.
      - `micronautEnv` values can be "prod" or "cstdevelop".
      - `releaseEnvironment` values can be "", "RELEASE", "STAGING", "DEVELOP", or any other environment represented by a matching folder in the GoldBox `ELIMINATION` path.
        - An empty string value will prevent the "Build deploy artifact" step and the entire `deploy-tarball` job (of the `build-middleware` workflow) from running.
      - `deployTargets` array values can be any internal testing/development server name (e.g. "cst143", "cst144", "cst145").
        - An empty list will prevent the "Trigger autopatch deploy" step (of the `deploy-tarball` job) from running.
    - Example:
      ```json
      {
        "default": {
          "micronautEnv": "",
          "releaseEnvironment": "",
          "deployTargets": []
        },
        "master": {
          "micronautEnv": "prod",
          "releaseEnvironment": "RELEASE",
          "deployTargets": []
        },
        "staging": {
          "micronautEnv": "cstdevelop",
          "releaseEnvironment": "STAGING",
          "deployTargets": ["cst145"]
        },
        "develop": {
          "micronautEnv": "cstdevelop",
          "releaseEnvironment": "DEVELOP",
          "deployTargets": ["cst143", "cst144"]
        }
      }
      ```
