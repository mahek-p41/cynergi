package com.hightouchinc.cynergi.test.data.loader

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
   private val companyDataLoaderService: CompanyDataLoaderService,
   private val organizationDataLoaderService: OrganizationDataLoaderService
) : ApplicationEventListener<ServerStartupEvent> {
   private val logger: Logger = LoggerFactory.getLogger(DemoDataLoader::class.java)

   override fun onApplicationEvent(event: ServerStartupEvent?) {
      logger.info("Creating demo data")

      val organization = organizationDataLoaderService.single()
      val company = companyDataLoaderService.single(organization)


      logger.info("Finished creating demo data")
   }
}
