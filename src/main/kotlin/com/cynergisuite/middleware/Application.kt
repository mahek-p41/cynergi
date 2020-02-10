package com.cynergisuite.middleware

import com.cynergisuite.middleware.load.develop.DevelopDataLoader
import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

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
      val mnEnvironment = systemProps["micronaut.environments"]

      if (mnEnvironment == "prod" && !systemProps.containsKey("logback.configurationFile")) {
         System.setProperty("logback.configurationFile", "logback-prod.xml")
      }

      val logger: Logger = LoggerFactory.getLogger(Application::class.java)
      val mn = Micronaut.build()
         .args(*args)
         .packages("com.cynergisuite.middleware")
         .mainClass(Application.javaClass)
         .start()

      try {
         if (mnEnvironment == "develop") {
            mn.getBean(DevelopDataLoader::class.java).loadDemoData() // FIXME when the loop that results with using this as a listener on the MigrationFinishedEvent
         }
      } catch (e: Throwable) {
         logger.error("Unhandled error occurred during data loading", e)
         exitProcess(-1)
      }
   }
}
