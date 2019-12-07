package com.cynergisuite.middleware

import com.cynergisuite.middleware.load.legacy.infrastructure.LegacyDataLoader
import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server

@OpenAPIDefinition(
   info = Info (
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

      if (systemProps["micronaut.environments"] == "prod" && !systemProps.containsKey("logback.configurationFile")) {
         System.setProperty("logback.configurationFile", "logback-prod.xml")
      }

      val mn = Micronaut.build()
         .args(*args)
         .packages("com.cynergisuite.middleware")
         .mainClass(Application.javaClass)
         .start()

      mn.getBean(LegacyDataLoader::class.java).processLegacyImports() // FIXME when the loop that results with using this as a listener on the MigrationFinishedEvent
   }
}
