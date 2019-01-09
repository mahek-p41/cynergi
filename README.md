# Cynergi Middleware

Provides the middleware component to the Java based Cynergi system.

## Setup
1. For Windows install [Git Bash](https://git-scm.com/download/win)
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
1. Make sure the database is running via *cynergi-dev-environment/cynergi-dev-middleware.sh*
2. Open up the `com.hightouchinc.cynergi.middleware.Application` class.
   1. Expand the src/main/kotlin source folder then navigate through the packages until you get to the `Application`
      class.
3. Once you have found the Application class in the "Project" explorer pane on the left side of the screen right click
   on the `Application` element and choose the "create 'com.hightouchinc.cyn..." selection
4. Inside the "Create Run/Debug Configuration: 'com.hightouchinc.cynergi.middleware.Application'" window make sure
   the following is correct or configured this way
   1. `Main class:` com.hightouchinc.cynergi.middleware.Application
   2. `VM options:` -Dmicronaut.environments=local
   3. You might want to give it a `Name:` like cynergi-middleware (local) at the top of the window
5. Once the runner has been created you can click the green arrow that points right to run the application or 
   the green bug looking thing to run in debug.  (The third option is to run in code coverage mode)

## To run from Command Line
Note: This option is useful if you just want to run the application but aren't interested in doing any coding.

1. Make sure the database is running via *cynergi-dev-environment/cynergi-dev-middleware.sh*
   1. Just leave this running in a separate terminal window
2. Change directory to the root of the *cynergi-middleware* project using a bash prompt (IE Git Bash on Windows)
   1. Will want to do this in a new terminal window separate from where the *cynergi-dev-middleware.sh* script is being run
3. Execute from the terminal `./gradlew clean shadowJar && java -Dmicronaut.environments=local -jar ./build/libs/cynergi-middleware*.jar`
4. To stop the application use `ctrl+c` AKA press the CTRL key at the same time you press the C key.


## Project Description

### Source Code locations

#### Deployable Source

##### Code 
Code that will be deployed with the final application is all housed in the `src/main/kotlin` directory.  All th files
in this directory will be compiled to Java bytecode via the Kotlin compiler that is configured in the gradle build
script.  The primary language used for writing the business logic is [Kotlin](https://kotlinlang.org/).

###### Code Conventions
1. Top Level Package *(namespace)*
   1. `com.hightouchinc.cynergi.middleware`
   2. All code will live under this package (and by extension directories on the file system)
2. Primary Subpackages - The packages are were the bulk of the code will reside.
   1. `entity`
      1. Start here.  The first thing that should be done is the design of the Java Class that will represent a single
         row in the database AKA an "Entity".
      1. This should be a [Kotlin Data Class](https://kotlinlang.org/docs/reference/data-classes.html) which are a nice
         way of defining
   2. `repository`
      1. Next define how the Entity will interact with the database via a Repository.  Place all SQL queries here as 
         well as the mapping
   3. `service`
   4. `validator`
   5. `controller`

##### Resources 
Resources such as application configuration is housed in `src/main/resources`.  Resources as defined in the scope
of this application are plain text files that are not compiled, but loaded by the application at runtime.  Examples of
these types of files are `src/main/resources/application.yml` or the SQL files in `src/main/resources/db/migration/postgres`.

