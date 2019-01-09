# Cynergi Middleware

Provides the middleware component to the Java based Cynergi system.

## Requirements
Need to checkout the cynergi-dev-environment project.

Once you have docker installed you can open up a command prompt and change directory to the `cynergi-dev-envrionemt`.  
Inside you'll find a `./cynerge-dev-middleware.sh`.  Execute this script to startup the main DB and the test DB before
you start this application.

## To run
Run the `com.hightouchinc.cynergi.middleware.Application` class.  Will need to pass in `-Dmicronaut.environments=local`


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

