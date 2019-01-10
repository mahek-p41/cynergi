# Cynergi Middleware

Provides the middleware component to the Java based Cynergi system.  All interactions from customers through to the
database will go through this application.

This project uses [Micronaut](http://micronaut.io/) as it's Enterprise framework.  It is similar to Spring and Spring
Boot in that it provides an annotation based develop workflow as well as various facilities to ease the develop cycle
such as a built-in HTTP server, Database connection pooling, beans validation and testing.

[Gradle](https://gradle.org/) is used as the build and dependency management tool.  It provides a declarative configuration
system that is intended to handle the 80% of a standard java development and deployment workflow.  You could easily use
Notepad or VIM to do your development as Gradle handles all the building outside of the development environment.

Currently Java 8 is the target for this application with the plan to move to Java 11 as soon as it is feasible.

## Setup
1. For Windows install [Git Bash](https://git-scm.com/download/win) and [Chocolatey](https://chocolatey.org/install)
   1. For Chocolatey the easiest way is to open up a Powershell Admin prompt and paste in 
      `Set-ExecutionPolicy Bypass -Scope Process -Force; iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))`
	2. Then we need the zip program so run `choco install zip`
2. Need to checkout the [cynergi-dev-environment](http://gitlab.hightouchinc.com/cynergi2019/cynergi-dev-environment) project.
3. Install [SDK Man](https://sdkman.io/)
   1. Easiest way is to open a bash terminal (Git Bash if you are on Windows)
      1. Make a backup of your bash profile files because sometimes the SDK Man install script seems to break bash on wndows
         1. `cp ~/.bashrc ~/.bashrc.old`
         2. `cp ~/.profile ~/.profile.old`
   2. Execute `curl -s "https://get.sdkman.io" | bash`
      1. There will be some instructions from the SDK Man installer about opening a new shell or sourcing a script.
         Either option is fine as long as we now have access to the `sdk` command.
   3. This does seem to cause a weird issue on Windows with destroying the original bash profile so you'll need to restore
      the copies made above if when you startup Git Bash it says it can't find a profile and has created a new one.
      You'll need to restore the `.old` from above and then append the required SDK Man config lines to the end of that 
      file which you should be able to do with the following command, making sure you put it in the right file.  The 
      command below points at .profile
```
cat > ~/.profile <<EOF
export SDKMAN_DIR="/c/Users/$USERNAME/.sdkman"
[[ -s "/c/Users/$USERNAME/.sdkman/bin/sdkman-init.sh" ]] && source "/c/Users/$USERNAME/.sdkman/bin/sdkman-init.sh"
EOF
```
4. Need to have at least a Java 8 SDK installed and on the path.
   1. Using SDK Man from the command line run `sdk install java 8.0.192-zulu`
      1. This will install an OpenJDK similar to what will be used when the application is deployed in production called
         Zulu that is built by a company called Azul Systems
      2. If you are on a Mac or Linux machine there are several other options to choose from that will give you Java 8
         but they have different features, but don't be afraid to experiment.  If you have something called **grl** or 
         **graalvm** it is recommended to use that as it has a different JIT that is more performant when code is
         written the way it is in the *cyerngi-middleware* project (meaning it uses Lambda's) and will therefore run 
         more efficiently.  
   2. Continuing from the command line execute `sdk use java 8.0.192-zulu`
      1. You may need to run this command on a regular basis if for some reason you attempt to run any of the java
         tooling and java command can't be found.  SDK Man should remember what the last JDK you instructed it to use
         was, but there have been times when it hasn't
      2. Also note for use with Intellij the JDK's that get installed via SDK Man are placed in the *$HOME/.sdkman/candidates/java/*
         directory.  To help with finding that you can from a bash terminal issue `which java` and note the overall
         directory structure SDK Man uses.
   3. To test out the installation run `java -version` which should print out something along the lines of
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
7. Configure a default JDK that Intellij will use
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
	     specific way.  Don't worry as Intellij checks to make sure it is valid before it makes that JDK available by 
	     verbally abusing you with a message like "No a valid JDK installation"
7. Click "OK" to save those configurations
9. Back on the "Welcome to Intellij IDEA" window click on the "Open" button in the middlish of the window
10. Choose the **cyerngi-middleware** project that you checked out from the terminal earlier.
11. Just take the defaults by clicking OK.
12. Finally the default builder for Intellij doesn't yet support some of the Micronaut features that are required by this
    project.  With that a change has to be made to Intellij to enable Gradle to handle all the building of the source
    code and the assembly of the resources.  If you don't do this the kotlin compiler won't generate the appropriate
    stubs that Micronaut uses to weave the application dependencies together.  You may have to uncheck this in other
    projects depending on how they are managed.
    1. File > Settings > Build, Execution, Deployment > Gradle > Runner
    2. Check the "Delegate IDE build/run actions to gradle"

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
The code and conventions defined here are meant to be eventually deployed to customers via a deployable artifact such as
a tarball or executable Jar.

##### Code 
Code that will be deployed with the final application is all housed in the `src/main/kotlin` directory.  All th files
in this directory will be compiled to Java bytecode via the Kotlin compiler that is configured in the gradle build
script.  The primary language used for writing the business logic is [Kotlin](https://kotlinlang.org/).

###### Code Conventions
1. Top Level Package *(namespace)*
   1. [com.hightouchinc.cynergi.middleware](./src/main/kotlin/com/hightouchinc/cynergi/middleware)
      1. All code will live under this package (and by extension directories on the file system)
2. Primary Subpackages - The packages are were the bulk of the code will reside.
   1. [Entity](./src/main/kotlin/com/hightouchinc/cynergi/middleware/entity)
      1. Start here.  The first thing that should be done is the design of the Java Class that will represent a single
         row in the database AKA an "Entity".
      2. These should be a [Kotlin Data Class](https://kotlinlang.org/docs/reference/data-classes.html) which are a nice
         way of defining simple classes that really only operate as data holders.  Advantages are that you get a nice
         _copy_ method that makes a copy of that data class and allows for the changing of values when that copy is done.
         Also _equals_, _hashCode_and _toString_ methods are generated in a standard way allowing for reduced maintenance
         burden on the developer.
         1. Additionally it is probably a good idea to define a data transfer object (aka DTO) that will be used to give
            shape to JSON responses to clients that interact with the HTTP endpoints.
         2. The simplest way to start out a DTO is to define another data class that is similar to the `entity`, but 
            has various Jackson and javax.validation annotations on it that will be evaluated during the web
            marshalling and unmarshalling process.
   2. [Repository](./src/main/kotlin/com/hightouchinc/cynergi/middleware/repository)
      1. Next define how the Entity will interact with the database via a Repository.  Place all SQL queries here as 
         well as the mapping code (usually via a Spring RowMapper implementation)
      2. To make dealing with JDBC easier the Spring Frameworks JdbcTemplate and NamedParameterJdbcTemplate have been
         configured in the container.  Simply express a dependency on one or both beans in the `@Inject constructor` to
         get access to them.
   3. [Service](./src/main/kotlin/com/hightouchinc/cynergi/middleware/service)
      1. It is now time to define the class(es) that will define the business logic that interacts with the `entity`
         defined earlier.
      2. All business logic should be housed in a `service` and should implement one of the two provided contract
         interfaces.  (Note: Most services defined in this project should be able to implement these interfaces)
         1. [com.hightouchinc.cynergi.middleware.service.CrudService](./src/main/kotlin/com/hightouchinc/cynergi/middleware/service/CrudService.kt) 
            when dealing with an `entity` that doesn't have a parent of some kind.  For example a company has no parent but a 
            store has a parent of a company that owns that store.  In this example a `CompanyService` would implement the 
            [CrudService](./src/main/kotlin/com/hightouchinc/cynergi/middleware/service/CrudService.kt)
            interface.
         2. [com.hightouchinc.cynergi.middleware.service.NestedCrudService](./src/main/kotlin/com/hightouchinc/cynergi/middleware/service/NestedCrudService.kt) 
            when dealing with an `entity` that does have a parent of some kind.  For example a company has no parent but 
            a store has a parent of a company that owns that store.  In this example a `StoreService` would implement 
            the [NestedCrudService](./src/main/kotlin/com/hightouchinc/cynergi/middleware/service/NestedCrudService.kt) 
            interface because it has a parent of a Company that will need to be provided for the
            business logic to manage the data in the database.
      3. If multiple interactions with the database are required it will be in the service where th is will be managed.
         That might also include one service depending on another such as the `StoreService` depending on some 
         functionality that is provided by the `CompanyService`
   4. [Controller](./src/main/kotlin/com/hightouchinc/cynergi/middleware/controller)
      1. The next to last class that will need to be created is a `controller`.
      2. All controllers should have two annotations at the top of them
         1. `io.micronaut.validation.Validated`
            1. This tells the Micronaut framework that HTTP Request Bodies should be validated against the javax.validation
               annotations defined on their properties.
            2. These annotations are used by the framework to determine if the payload from the HTTP client is even
               somewhat close to valid, and is the first line of defense the API uses to ensure data integrity 
         2. `io.micronaut.http.annotation.Controller`
            1. This annotation tells the Micronaut framework that public methods defined in this class and annotated
               with annotations such as `io.micronaut.http.annotation.Get` and `io.micronaut.http.annotation.Post` will
               provide HTTP endpoints that can be interacted with via an HTTP client of some kind such as cURL or 
               a web browser.
            2. A `value` will need to be provided that is the HTTP path through which the methods defined on this call
               can be reached.  This `value` on the `Controller` annotation is the root of the path for this method.
               The HTTP verb annotations also can define paths via the `value` attribute which will be appended to the
               path defined on the `value` of the `Controller` annotation.
      3. Controllers are how the application is interacted with and as such act as routers to the business logic that 
         are housed in one or more `service` implementations.  A good rule of thumb is that it will be a one-to-one 
         mapping of `controller` to `service`
      4. Controllers also act as the gatekeeper to the business logic applying all validation first through the
         standardized javax.validation system, and second through validators.
   5. [Validator](./src/main/kotlin/com/hightouchinc/cynergi/middleware/validator)
      1. Finally a `validator` will need to be defined.  This validator is the next line of defense that the API will
         use to protect data integrity typically by checking that states are valid based on the data being passed. 
      2. There are two types of validators that can be defined.  The final line of defense are the constraints defined
         in the database via check constraints foreign keys, other method...
         1. [Validator](./src/main/kotlin/com/hightouchinc/cynergi/middleware/validator/Validator.kt)
            1. This defines a top level validator for the an entity that has no parent.
         1. [NestedValidator](./src/main/kotlin/com/hightouchinc/cynergi/middleware/validator/NestedValidator.kt)
            1. This defines a validator for an entity that has a parent.
      3. There will always need to be at the very least a `validateSave` and a `validateUpdate` method implemented
         1. `validateSave` will need to be called by the controller when a POST is made to the server that will be 
            creating new rows in the database and as such won't have an ID assigned to them yet.
         2. `validateUpdate` will need to be called by the controller when a PUT is made to the server that will
            be updating rows in the database.
      4. Additional methods can be defined on implementations that need to different validation other than the basic
         CREATE and UPDATE processes.
      5. Also user validation can be done here to determine if a user has access to be applying the changes they are 
         attempting to apply.  The current interfaces will need to be extended to handle user validation as they have
         not currently been defined with that ability.

##### Resources 
Resources as defined in the scope of this application are plain text files that are not compiled, but loaded by the 
application at runtime. Resources such as application configuration is housed in [Resources](./src/main/resources).  
Examples of these types of files are [application.yml](src/main/resources/application.yml) or the 
[SQL Migrations](src/main/resources/db/migration/postgres) files.

#### Testing Source
The source code described here will never be deployed to a customer or production system as it is meant to test code that
will ultimately be deployed.  The testing framework used by this project is called [Spock](http://spockframework.org).
It is a Behavior Driven Testing tool that makes writing tests easier, especially when it comes to mocking out dependencies.

There are going to be two types of tests described here.  

##### Unit Tests
A simple unit test that the developer will use to individually test out small parts of the business logic under various 
scenarios.  In a unit test much of the infrastructure is "mocked" out to better control the inputs and outputs to the 
business logic being testing.  Typically these tests are going to be small and fast.

##### Integration Tests
A second type of test is a functional test that will involve spinning up the entire application pointed at a locally
running instance of Postgres (and any other 3rd party dependencies), and make real HTTP calls through the HTTP endpoints
defined by the `@Controller`'s to determine if the result is correct.  These tests will have the most value when it comes
to determining if a feature can be shipped as they will define the contract the API is providing, and checking that the
API is actually fulfilling that contract.
