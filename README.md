# Cynergi Middleware

Provides the middleware component to the Java based Cynergi system.

## Setup
1. For Windows install Git Bash
2. Need to checkout the [cynergi-dev-environment](http://gitlab.hightouchinc.com/cynergi2019/cynergi-dev-environment) project.
3. Install [SDK Man](https://sdkman.io/)
   1. Easiest way is to open a bash termail (Git Bash if you are on Windows)
   2. Execute `curl -s "https://get.sdkman.io" | bash`
      1. There will be some instructions from the SDK Man installer about opening a new shell or sourcing a script.
         Either option is fine as long as we now have access to the sdk command.
4. Need to have at least a Java 8 SDK installed and on the path.
   1. Using SDK Man from the command line run `sdk install java 8.0.192-zulu`
      1. This will install an OpenJDK similar to what will be used when the application is deployed in production called
         Zulu that is built by a company called Azul Systems
      2. If you are on a Mac of Linux machine there are several other options to choose from that will give you Java 8
         but they have different features, but don't be afraid to experiment.  If you have something called **grl** or 
         **graalvm** it is recommended to use that as it has a different JIT that is more performant when code is
         written the way it is in the *cyerngi-middleware* project and will therefore run more efficiently.  
   2. Continuing from the command line execute `sdk use java 8.0.192-zulu`
      1. You may need to run this command on a regular basis if for some reason you attempt to run any of the java
         tooling and java command can't be found.  SDK Man should remember what the last JDK you instructed it to use
         was, but there have been times when it hasn't
      2. Also note for use with Intellij the JDK's that get installed via SDK Man are placed in the *$HOME/.sdkman/candidates/java/*
         directory.
   3. To test out the installation run `java -version` which should print out
      ```
      openjdk version "1.8.0_192"
      OpenJDK Runtime Environment (Zulu 8.33.0.1-win64) (build 1.8.0_192-b01)
      OpenJDK 64-Bit Server VM (Zulu 8.33.0.1-win64) (build 25.192-b01, mixed mode)
      ```
5. Install [Intellij](https://www.jetbrains.com/idea/download)
   1. If you have an Intellij Ultimate License feel free to install and use that
   2. If you don't have an Intellij Ultimate License you'll want to grab the Community Edition.
      1. Ultimate Edition has nicer tools in terms of built-in database connectivity, better code analysis and different
         framework support.  Non of that is strictly necessary and the Community Edition is perfectly fine for this
         project.
6. Start Intellij
7. Configure a default JDK that Intelij will use
   1. Assuming you have never used Intellij before it will come up with the "Welcome to Intellij IDEA" screen
   2. From the "Welcome" screen down in the lower right hand corner there is a "Configure" drop-down
   3. Click "Configure > Project Defaults > Project Structure" which will bring up the "Project Structure for New Projects"
      window
   4. On the left hand side select the "Project Settings > Project" selection which will activate the Project SDK and Language
      config screen
   5. Under the "Project SDK" section the JDK needs to be configured.  (There may already be one configured or it might say "<NO SDK>")
   6. Click the "New... > +JDK" button next to the JDK selection drop-down
   7. Navigate to where the JDK was installed from earlier and choose the directory that is where you pointed your JAVA_HOME
      environment variable from Bash earlier.  On Windows it will be something like **C:\Users\whoami\.sdkman\candidates\java\8.0.192-zulu**
	  1. When choosing this make sure you choose the root of the directory as the way the tooling works the JDK is laid out in a 
	     specific way.  Don't worry as Intellij checks to make sure it is valid before it makes that JDK available.
7. Click "OK" to save those configurations
9. Back on the "Welcome to Intellij IDEA" window click on the "Open" button in the middlish of the window
10. Choose the **cyerngi-middleware** project that you checked out from the terminal earlier.
11. Just take the defaults by clicking OK.  And you should be ready to develop on the source code.

## To run from Intellij
Run the `com.hightouchinc.cynergi.middleware.Application` class.  Will need to pass in `-Dmicronaut.environments=local`
as a jvm argument

## To run from Command Line
1. Make sure the database is running via *cynergi-dev-environment/cynergi-dev-middleware.sh*
   1. Just leave this running in a separate terminal window
2. Change directory to the root of the *cynergi-middleware* project using a bash prompt (IE Git Bash on Windows)
   1. Will want to do this in a new terminal window separate from where the *cynergi-dev-middleware.sh* script is being run
3. Execute from the terminal `./gradlew clean shadowJar && java -Dmicronaut.environments=local -jar ./build/libs/cynergi-middleware*.jar`
4. To stop the application use `ctrl+c` AKA press the CTRL key at the same time you press the C key.
