# Cynergi Middleware

Provides the middleware component to the Java based Cynergi system.  All interactions from customers through to the
database will go through this application.

This project uses [Micronaut](http://micronaut.io/) as it's Enterprise framework.  It is similar to Spring and Spring
Boot in that it provides an annotation based develop workflow as well as various facilities to ease the develop cycle
such as a built-in HTTP server, Database connection pooling, beans validation and testing.

[Gradle](https://gradle.org/) is used as the build and dependency management tool.  It provides a declarative configuration
system that is intended to handle the 80% of a standard java development and deployment workflow.  You could easily use
Notepad or VIM to do your development as Gradle handles all the building outside of the development environment.

## Documentation
* [Coding Standards](docs/coding-standard.md)
* [Relational Database Design guidelines](docs/relational-database-design-guidlines.md)
* [Windows setup](docs/windows-setup.md)
* [Mac Setup](docs/mac-setup.md)

## Development environment
[Direnv](https://direnv.net/) is used by cynerig-middleware to put a `cyn` command on your path whenever you change
directory into __cynergi-middleware__ via your terminal.  `cyn` is simply a bash script located in the __support/.cyn/__
directory and does pattern matching to to sub-scripts to provide functionality.  The `cyn` command searches under the
__cynergi-middleware/support/cyn/commands__ directory for sub-commands to execute.  Each directory nesting is itself
another sub-command.  Pattern matching is used, which means that as long as the collection of characters leading up and
including a sub-command are unique the `cyn` command will know what script to execute.

1. `cyn` with no arguments will give a listing of possible sub-commands tha can be called.
   1. The above should give you something along the lines of
      ```shell script
      $ cyn
      Script: cleanup.sh
      Script: stop.sh
      Dir: db
      Dir: middleware
      ```
   2. If it says `Script: ` then all you need to do is provide that after the `cyn` command
      1. For example `cyn stop` will execute the __cynergi-middleware/support/cyn/commands/stop.sh__ shell script

## Run the database via cyn command hosted by Docker


### Local Database
To start the local database `cyn db start dev`

The Local database runs Postgres 9.3 via docker with 2 databases.  One is the **cynergidb** which is intended to survive
restarts of the cynergi-middleware application.  The other is the **cynergidevelopdb** and is intended to be refreshed by the
cynergi-middleware everytime it restarts.  This is as close to running production as can be achieved.  The databases
require fastinfo dumps from a CST or production machine to operate.


#### cynergidb
Local semi-persistent PostgreSQL database hosted by docker.  States of the database can be
captured via the pg_dump command.
1. Connection information
   1. port: 6432
   2. host: localhost
   3. database: cynergidb
   4. user: postgres
   5. password: password
2. Connecting to the database via psql
   1. `cyn db psql dev cynergidb`

#### cynergidevelopdb
1. Connection Information
   1. port: 6432
   2. host: localhost
   3. database: cynergidevelopdb
   4. user: postgres
   5. password: password
2. Connecting to the database via psql
   1. `cyn db psql dev cynergidevelopdb`

#### Dumps
1. cynergidb.dump
   1. This dump will be read when the db is started up if it is available.
   2. If this file doesn't exist the cynergidb database will be empty after each successive restart of the docker
      container
2. fastinfo.dump
   1. This dump will be read when the db is started up.  It captures the state of fastinfo from an external source.
      The external source is most likely a CST machine (probably CST 137).  If this dump does not exist the database
      initialization scripts make a best effort to create tables that will stand-in for the tables required by the
      *cynergi-middleware*.
   2. If this file doesn't exist the fastinfo_production database will most likely not start up.

### Test Database
A separate test database is used for integration testing via the Micronaut testing harness.  It runs in memory to
do quicker loading and unloading of test data during a test run.  This database does not provide the ability to
read snapshots of any kind as it is intended to be completely ephemeral.

To start the local database `cyn db start test`

1. Connection information
   1. port: 7432
   2. host: localhost
   3. database: cynergidb
   4. user: postgres
   5. password: password
2. Connecting to the database via psql
   1. `cyn db psql test`

## To run the application from Intellij
1. Make sure the database is running via `cyn db start dev`
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

1. Make sure the database is running via `cyn db start dev`
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

##### Coding Conventions

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
1. Generate API doc locally
   * `./gradlew buildApiDocs`
   * This will put a file in [./build/reports/openapi/index.html](./build/reports/openapi/index.html) that you can
     open with a browser.
2. Run migrations against the cynergidb database
   * `./gradlew flywayMigrateCynergiDb`
3. Run migrations against the cynergidevelopdb database
   * `./gradlew flywayMigrateCynergiDevelopDb`
2. Run migrations against the cynergitestdb database
   * `./gradlew flywayMigrateCynergiTestDb`
