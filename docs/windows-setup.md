# Basic Windows setup
1. Install [Chocolatey](https://chocolatey.org/) using their instructions
2. Install Java from an admin prompt using `choco install adoptopenjdk8openj9`
3. Install [Docker for Windows](https://www.docker.com/products/docker-desktop)
4. Install [Windows Subsystem for Linux](https://docs.microsoft.com/en-us/windows/wsl/about) by following their
   provided instructions.  Just use Ubuntu as it is the most supported by Microsoft at the moment.
5. Install [Sdkman](https://sdkman.io/) inside of your WSL environment once you have finished step 3.
6. Install [direnv](https://direnv.net/) inside of your WSL environment once you have finished step 3.
   1. `sudo apt install direnv`
   2. Make sure to add direnv to your shell's profile.
      1. If you are using bash put `eval "$(direnv hook bash)"` at the end of __$HOME/.profile__ file
      2. If you are using zsh put `eval "$(direnv hook zsh)"` at the end of your __$HOMe/.zshrc__ file
   3. direnv is used in the cynergi-middleware project to make a collection of tools available to your terminal.
7. Install Java using Sdkman inside your WSL environment
   1. `sdk install java 8.0.242.j9-adpt`
