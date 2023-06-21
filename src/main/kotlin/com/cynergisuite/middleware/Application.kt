package com.cynergisuite.middleware

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server
import java.net.InetAddress

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
      val isCst = try {
         (InetAddress.getLocalHost().hostName.startsWith("cst"))
      } catch(e: Throwable) {
         false
      }

      if ((mnEnvironment == "prod" || mnEnvironment == "cstdevelop") && !systemProps.containsKey("logback.configurationFile")) {
         System.setProperty("logback.configurationFile", "logback-prod.xml")
      }


      if (isCst) {
         if (mnEnvironment != "cstdevelop") {
            System.setProperty("micronaut.environments", "cst")
         }

         System.setProperty("logback.configurationFile", "logback-cst.xml")
      }

      Micronaut.build()
         .eagerInitSingletons(true)
         .args(*args)
         .packages("com.cynergisuite.middleware")
         .mainClass(Application.javaClass)
         .start()
   }
}
