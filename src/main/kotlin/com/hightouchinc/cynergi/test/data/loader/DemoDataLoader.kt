package com.hightouchinc.cynergi.test.data.loader

import com.hightouchinc.cynergi.middleware.repository.AreaRepository
import com.hightouchinc.cynergi.middleware.repository.MenuRepository
import com.hightouchinc.cynergi.middleware.repository.ModuleRepository
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
   private val areaRepository: AreaRepository,
   private val companyDataLoaderService: CompanyDataLoaderService,
   private val menuRepository: MenuRepository,
   private val moduleRepository: ModuleRepository,
   private val organizationDataLoaderService: OrganizationDataLoaderService
) : ApplicationEventListener<ServerStartupEvent> {
   private val logger: Logger = LoggerFactory.getLogger(DemoDataLoader::class.java)

   override fun onApplicationEvent(event: ServerStartupEvent?) {
      logger.info("Creating demo data")

      val organization = organizationDataLoaderService.single()
      val company = companyDataLoaderService.single(organization)

      logger.info("Setting up AP for {}", company)
      val apMenu = menuRepository.findOne("AP")!!
      val apModules = moduleRepository.findAllAssociatedWithMenu(apMenu)
      val apArea = areaRepository.associate(apMenu, company, 2)
      // TODO associate modules to company with proper level

      logger.info("Setting up APRECR for {}", company)
      val aprecurMenu = menuRepository.findOne("APRECUR")!!
      val aprecurModules = moduleRepository.findAllAssociatedWithMenu(aprecurMenu)
      val aprecurArea = areaRepository.associate(aprecurMenu, company, 2)
      // TODO associate modules to company with proper level

      logger.info("Setting up APREPORT for {}", company)
      val apreportMenu = menuRepository.findOne("APREPORT")!!
      val apreportModules = moduleRepository.findAllAssociatedWithMenu(apreportMenu)
      val apreportArea = areaRepository.associate(aprecurMenu, company, 2)
      // TODO associate modules to company with proper level


      logger.info("Finished creating demo data")
   }
}
