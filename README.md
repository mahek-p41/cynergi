# Cynergi Middleware

Provides the middleware component to the Java based Cynergi system.  All interactions from customers through to the
database will go through this application.

This project uses [Micronaut](http://micronaut.io/) as it's Enterprise framework.  It is similar to Spring and Spring
Boot in that it provides an annotation based develop workflow as well as various facilities to ease the develop cycle
such as a built-in HTTP server, Database connection pooling, beans validation and testing.

[Gradle](https://gradle.org/) is used as the build and dependency management tool.  It provides a declarative configuration
system that is intended to handle the 80% of a standard java development and deployment workflow.  You could easily use
Notepad or VIM to do your development as Gradle handles all the building outside of the development environment.

## Setup
1. For Windows install follow instructions at [Java Dev Setup](http://gitlab.hightouchinc.com/garym/java-dev-setup)
   1. This should have installed __adoptopenjdk8openj9__, if for some reason it did not open an admin
      Powershell window and run `choco install adoptopenjdk8openj9`
2. Configure a default JDK that Intellij will use
   1. Assuming you have never used Intellij before it will come up with the "Welcome to Intellij IDEA" screen
   2. From the "Welcome" screen down in the lower right hand corner there is a "Configure" drop-down
   3. Click "Configure > Project Defaults > Project Structure" which will bring up the "Project Structure for New Projects"
      window
   4. On the left hand side select the "Project Settings > Project" selection which will activate the Project SDK and Language
      config screen
   5. Under the "Project SDK" section the JDK needs to be configured.  (There may already be one configured or it might say "<NO SDK>")
   6. Click the "New... > +JDK" button next to the JDK selection drop-down
   7. Navigate to where the JDK was installed from earlier and choose the directory that is where you pointed your JAVA_HOME
      environment variable from Bash earlier.  On Windows it will be something like **C:\Program Files\AdoptOpenJDK\jdk-someversion-openj9**
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

## Run the database via Docker

### Local Database
The Local database runs Postgres 9.3 via docker with 2 databases.  One is the **cynergidb** which is intended to survive
restarts of the cynergi-middleware application.  The other is the **cynergidevelopdb** and is intended to be refreshed by the
cynergi-middleware everytime it restarts.

To run the local databases change directory to the */support* directory and execute the `./cynergi-dev-db.sh` script.

#### cynergidb
Local semi-persistent PostgreSQL database hosted by docker.  States of the database can be
captured via the pg_dump command.
1. Connection information
   1. port: 6432
   2. host: localhost
   3. database: cynergidb
   4. user: postgres
   5. password: password
2. Capturing snapshots
   1. Within the */support* directory run the `./cynergi-dev-db-snapshot.sh` script
   2. Snapshots are placed in the */support/docker/db/DatabaseDumps* directory
3. Connecting to the database via psql
   1. Within the */support* directory run the `./cynergi-dev-db-psql.sh` script
   2. This script by default will connect you to the cynergidb database as the postgres user.

#### cynergidevelopdb
1. Connection Information
   1. port: 6432
   2. host: localhost
   3. database: cynergidevelopdb
   4. user: postgres
   5. password: password

#### Managing Database state
The scripts that manage the docker database manage states through restarts by reading for dump files housed in the
*/support/docker/db/DatabaseDumps* directory.

##### Dumps
1. cynergidb.dump
   1. This dump will be read when the db is started up.  It captures the previous state from when the
      `./cynergi-dev-db-snapshot.sh` script was ran.
   2. If this file doesn't exist the cynergidb database will be empty after each successive restart of the docker
      container
2. fastinfo.dump
   1. This dump will be read when the db is started up.  It captures the state of fastinfo from an external source.
      The external source is most likely a CST machine (probably CST 137).  If this dump does not exist the database
      initialization scripts make a best effort to create tables that will stand-in for the tables required by the
      *cynergi-middleware*.
   2. If this file doesn't exist the fastinfo_production database will be put into a basic state that full fills the
      requirements of the cynergi-middleware SQL code.

### Test Database
A separate test database is used for integration testing via the Micronaut testing harness.  It runs in memory to
do quicker loading and unloading of test data during a test run.  This database does not provide the ability to
read snapshots of any kind as it is intended to be completely ephemeral.

To run the local databases change directory to the */support* directory and execute the `./cynergi-test-db.sh` script.

1. Connection information
   1. port: 7432
   2. host: localhost
   3. database: cynergidb
   4. user: postgres
   5. password: password
2. Connecting to the database via psql
   1. Within the */support* directory run the `./cynergi-test-db-psql.sh ` script
   2. This script by default will connect you to the cynergitestdb database as the postgres user.

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
3. For local mode execute from the terminal
   1. `./gradlew clean shadowJar && java -Dmicronaut.environments=local -jar ./build/libs/cynergi-middleware*-all.jar`
4. For develop mode execute from the terminal
   1. `./gradlew clean shadowJar && java -Dmicronaut.environments=develop -jar ./build/libs/cynergi-middleware*-all.jar`
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

##### Coding Conventions TODO

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

## Helpers
* `./gradlew clean assemble openApiGenerate`

## Support Scripts (deprecated)
This project provides several scripts to make it easier to interact with the different Docker hosted databases.

### Convention used for naming scripts
There is a convention for these scripts that is hierarchical in nature starting with a root.
1. `cynergi` - Intended as the "root" of a command structure
1. `cleanup` - provides a cleanup functionality
1. `db` - Will interact with one of the provided databases
   1. `database`- Will interact with a defined database environment
      1. `subcommand` - Execute a type of subcommand against that database environment
         * If there is no environment specified in the script name before the `subcommand` to execute is provided it will be executed against the default database

### Scripts
1. `cynergi-db.sh` - Starts a Postgres Docker container that will host cynergidb, cynergidevelopdb, and fastinfo_production
1. `cynergi-db-psql.sh` - Starts a Docker container with a psql prompt connected to the cynergidb database
1. `cynergi-db-snapshot.sh` - Runs a Docker container that will execute a pg_dump against the cynergidb database and store the result in /support/development/db/DatabaseDumps/cynergidb.dump
1. `cynergi-db-develop-psql.sh` - Starts a Docker container with a psql prompt connected to the cynergidevelopdb database
1. `cynergi-db-fastinfo-snapshot.sh` - Runs a Docker container that will execute a pg_dump against the fastinfo_production database and store the result in /support/develoopment/db/DatabaseDumps/fastinfo.dump
1. `cynergi-db-test.sh` - Starts a Postgres Docker container that will host the cynergitestdb and fastinfo_production databases in memory
1. `cynergi-db.-test-psql.sh` - Starts a Docker container with the psql prompt connected to the cynergidtestdb database
