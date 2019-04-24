package com.cynergisuite.middleware

import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Requires(env = ["demo", "test"])
class DemoDataLoader @Inject constructor(

) : ApplicationEventListener<ServerStartupEvent> {
   private val logger: Logger = LoggerFactory.getLogger(DemoDataLoader::class.java)

   override fun onApplicationEvent(event: ServerStartupEvent?) {
      logger.info("Creating demo data")

      logger.info("Finished creating demo data")
   }
}
