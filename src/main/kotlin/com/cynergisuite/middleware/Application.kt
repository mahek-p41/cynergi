package com.cynergisuite.middleware

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server

@OpenAPIDefinition(
   info = Info(
      title = "Cynergi Middleware",
      description = "Cynergi Middleware API"
   ),
   servers = [
      Server(
         url = "/",
         description = "cynergi-middleware"
      )
   ]
)
object Application {

   @JvmStatic
   fun main(args: Array<String>) {
      val systemProps = System.getProperties()
      val mnEnvironment = systemProps["micronaut.environments"]

      if ((mnEnvironment == "prod" || mnEnvironment == "cstdevelop") && !systemProps.containsKey("logback.configurationFile")) {
         System.setProperty("logback.configurationFile", "logback-prod.xml")
      }

      Micronaut.build()
         .eagerInitSingletons(true)
         .args(*args)
         .packages("com.cynergisuite.middleware")
         .mainClass(Application.javaClass)
         .start()
   }
}
