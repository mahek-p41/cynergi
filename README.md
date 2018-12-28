# Cynergi Middleware

Provides the middleware component to the Java based Cynergi system.

## Requirements
Need to checkout the cynergi-dev-environment project.

Once you have docker installed you can open up a command prompt and change directory to the `cynergi-dev-envrionemt`.  
Inside you'll find a `./cynerge-dev-middleware.sh`.  Execute this script to startup the main DB and the test DB before
you start this application.

## To run
Run the `com.hightouchinc.cynergi.middleware.Application` class.  Will need to pass in `-Dmicronaut.environments=local`
