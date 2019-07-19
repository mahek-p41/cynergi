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
      environment variable from Bash earlier.  On Windows it will be something like **C:\Program Files\AdoptOpenJDK\jdk8u-somethingorother**
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
restarts of the cynergi-middleware application.  The other is the **cynergidemodb** and is intended to be refreshed by the
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
   
##### Some test data
Within the */support/docker/db/DatabaseDumps* directory there are some files that you can use to get some test data
if you don't want to deal with or don't have a fastinfo_production dump.

1. stores.csv
   1. Open a psql prompt by running the `./cynergi-dev-db-psql.sh`
   2. Run the following SQL
      ```sql
      \c fastinfo_production
      DELETE FROM corrto.level2_stores;
      COPY corrto.level2_stores(loc_tran_loc,loc_transfer_desc) 
      FROM '/tmp/dumps/stores.csv' DELIMITER ',' CSV HEADER;
      ```
2. employees.csv
   1. Open a psql prompt by running the `./cynergi-dev-db-psql.sh`
   2. Run the following SQL
      ```sql
      \c fastinfo_production
      DELETE FROM corrto.level1_loc_emps;
      COPY corrto.level1_loc_emps(emp_nbr,emp_last_name,emp_first_name_mi,emp_pass_1,emp_pass_2,emp_pass_3,emp_pass_4,emp_pass_5,emp_pass_6,emp_store_nbr) 
      FROM '/tmp/dumps/employees.csv' DELIMITER ',' CSV HEADER;
      ```
3. products.csv
   1. Open a psql prompt by running the `./cynergi-dev-db-psql.sh`
   2. Run the following SQL
      ```sql
      \c fastinfo_production
      DELETE FROM corrto.level1_ninvrecs;
      COPY corrto.level1_ninvrecs(inv_serial_nbr_key, inv_alt_id, inv_location_rec_1, inv_status, inv_mk_model_nbr, inv_model_nbr_category, inv_desc) 
      FROM '/tmp/dumps/products.csv' DELIMITER ',' CSV HEADER;
      ```

#### cynergidemodb
1. Connection Information
   1. port: 6432
   2. host: localhost
   3. database: cynergidemodb
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
facilitate quicker loading and unloading test data during a test run.  This database does not provide the ability to 
read snapshots of any kind as it is intended to be ephemeral. 

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
4. For demo mode execute from the terminal 
   1. `./gradlew clean shadowJar && java -Dmicronaut.environments=demo -jar ./build/libs/cynergi-middleware*-all.jar`
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
      2. All business logic should be housed in a `service` and should implement the following interface
         1. [com.hightouchinc.cynergi.middleware.service.IdentifiableService](./src/main/kotlin/com/hightouchinc/cynergi/middleware/service/IdentifiableService.kt) 
            when dealing with an `entity` that has a primary key that is auto incrementing and some sort of integral type.
      3. If multiple interactions with the database are required it will be in the service where this will be managed.
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
      2. There will always need to be at the very least a `validateSave` and a `validateUpdate` method implemented
         1. `validateSave` will need to be called by the controller when a POST is made to the server that will be 
            creating new rows in the database and as such won't have an ID assigned to them yet.
         2. `validateUpdate` will need to be called by the controller when a PUT is made to the server that will
            be updating rows in the database.
      3. Additional methods can be defined on implementations that need to different validation other than the basic
         CREATE and UPDATE processes.
      4. Also user validation can be done here to determine if a user has access to be applying the changes they are 
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

## Helpers
* `npx yo cyn:entity new_table`
  * Will generate 
    * NewTableEntity.kt
    * NewTableRepository.kt 
    * NewTableTestDataloader.groovy
* `npx yo cyn:controller new_table`
  * Will generate
    * NewTableController.kt
    * NewTableService.kt
    * NewTableValidator.kt
    * NewControllerSpecification.groovy
* `npx yo cyn:migration new_table feature-new-stuff-cynXXX`
  * Will create a migration file by calculating the version of the file based on the highest existing version with the
    naming convention of _src/main/resources/db/migration/postgres/V${versionNumber}__${description}.sql_
    * The version number is generated by iterating though the files in the _src/main/resources/db/migration/postgres_
      directory and adding 1 to the highest named file
    * The file it creates will have the skeleton of the new table to be created.  On the 4 general columns of
      1. id - BIGSERIAL
      2. uu_row_id - UUID - DEFAULT uuid_generate_v1()
      3. time_created - TIMESTAMPZ - DEFAULT clock_timestamp()
      4. time_updated - TIMESTAMPZ - DEFAULT clock_timestamp()
    * Also a trigger will be attached to the table to keep the time_updated column up-to-date
* `npx yo cyn:migration new_table feature-new-stuff-cynXXX -a`
  * Will append the table skeleton described above to an already existing migration script rather than creating a new one
* `TABLE_NAME=new_table bash -c 'npx yo cyn:entity $TABLE_NAME && npx yo cyn:controller $TABLE_NAME && npx yo cyn:migration $TABLE_NAME feature-new-stuff-cynXXX'`
* `./gradlew clean assemble openApiGenerate`
