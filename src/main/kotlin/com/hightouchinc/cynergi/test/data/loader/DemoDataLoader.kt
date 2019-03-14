package com.hightouchinc.cynergi.test.data.loader

import com.hightouchinc.cynergi.middleware.repository.AreaRepository
import com.hightouchinc.cynergi.middleware.repository.CompanyModuleAccessRepository
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
   private val companyModuleAccessDataLoaderService: CompanyModuleAccessDataLoaderService,
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
      val apCompanyModules = companyModuleAccessDataLoaderService.associate(companyIn = company, moduleLevels = listOf(
         "APADD" to 10,
         "APCHG" to 30,
         "APDEL" to 30,
         "APSTATUS" to 10,
         "APUTIL" to 10,
         "APRECUR" to 10,
         "APGLRPT" to 40,
         "APCHKLST" to 20,
         "APLST" to 10,
         "APCHECK" to 40,
         "APREPORT" to 10,
         "APSEL" to 30,
         "APSELDUE" to 30,
         "APSHO" to 10,
         "APVOID" to 60,
         "PRTAPDST" to 0
      ))

      logger.info("Setting up APRECR for {}", company)
      val aprecurMenu = menuRepository.findOne("APRECUR")!!
      val aprecurModules = moduleRepository.findAllAssociatedWithMenu(aprecurMenu)
      val aprecurArea = areaRepository.associate(aprecurMenu, company, 2)
      val aprecurCompanyModules = companyModuleAccessDataLoaderService.associate(companyIn = company, moduleLevels = listOf(
         "ADDAPREC" to 10,
         "CHGAPREC" to 30,
         "DELAPREC" to 40,
         "LSTAPREC" to 10,
         "PRTAPREC" to 20,
         "SHOAPREC" to 10,
         "TRNAPREC" to 30
      ))

      logger.info("Setting up APREPORT for {}", company)
      val apreportMenu = menuRepository.findOne("APREPORT")!!
      val apreportModules = moduleRepository.findAllAssociatedWithMenu(apreportMenu)
      val apreportArea = areaRepository.associate(aprecurMenu, company, 2)
      val apreportCompanyModules = companyModuleAccessDataLoaderService.associate(companyIn = company, moduleLevels = listOf(
         "APAGERPT" to 10,
         "FLOWANAL" to 10,
         "APCHKRPT" to 20,
         "APEXPENS" to 20,
         "APRPT" to 20,
         "APPREVUE" to 30,
         "CASHOUT" to 20,
         "APTRLBAL" to 30,
         "AP1099" to 20,
         "APUNDO" to 10
      ))


      logger.info("Finished creating demo data")
   }
}
