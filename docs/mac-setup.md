# Mac setup
1. Install [Docker for Mac](https://www.docker.com/products/docker-desktop)
2. Install [Homebrew](https://brew.sh/)
3. Install Install [Sdkman](https://sdkman.io/)
4. Install [direnv](https://direnv.net/) after installing Homebrew in step 2.
   1. `brew install direnv`
   2. Make sure to add direnv to your shell's profile.
      1. If you are using bash put `eval "$(direnv hook bash)"` at the end of __$HOME/.profile__ file
      2. If you are using zsh put `eval "$(direnv hook zsh)"` at the end of your __$HOMe/.zshrc__ file
   3. direnv is used in the cynergi-middleware project to make a collection of tools available to your terminal.
5. Install Java using Sdkman
   1. `sdk install java 8.0.242.j9-adpt`
