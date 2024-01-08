# Mac setup
1. Install [Docker for Mac](https://www.docker.com/products/docker-desktop)
   1. Configure Docker Daemon(Docker Engine tab) to have more `defaultKeepStorage` if the database is unable to spin up from big dump files:
   ```{
   "builder": {
   "gc": {
   "defaultKeepStorage": "10GB",
   "enabled": true
   }
   },
   "experimental": false
   }```
2. Install [Homebrew](https://brew.sh/)
3. Install Install [Sdkman](https://sdkman.io/)
4. Install [direnv](https://direnv.net/) after installing Homebrew in step 2.
   1. `brew install direnv`
   2. Make sure to add direnv to your shell's profile.
      1. If you are using bash put `eval "$(direnv hook bash)"` at the end of __$HOME/.profile__ file
      2. If you are using zsh put `eval "$(direnv hook zsh)"` at the end of your __$HOMe/.zshrc__ file
      3. Execute `direnv allow` in the terminal to make direnv work.
   3. direnv is used in the cynergi-middleware project to make a collection of tools available to your terminal.
5. Install Java using Sdkman
   1. `sdk install java 11.0.21-tem`. Build number part `21` is for bug fixes & security updates, so it could be the latest build.
6. Install [IntelliJ](https://www.jetbrains.com/idea)
   1. The maximum heap size needs to be increased in some machines in order to run the whole test suite successfully.
