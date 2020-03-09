package com.cynergisuite.middleware.load.develop

import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.detail.AuditDetailFactoryService
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactoryService
import com.cynergisuite.middleware.audit.exception.AuditExceptionFactoryService
import com.cynergisuite.middleware.audit.schedule.AuditScheduleFactoryService
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.division.DivisionFactoryService
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.region.RegionFactoryService
import com.cynergisuite.middleware.store.StoreFactoryService
import io.micronaut.context.annotation.Requires
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Requires(env = ["develop"])
class DevelopDataLoader @Inject constructor(
   private val auditDetailFactoryService: AuditDetailFactoryService,
   private val auditExceptionFactoryService: AuditExceptionFactoryService,
   private val auditFactoryService: AuditFactoryService,
   private val auditScanAreaFactoryService: AuditScanAreaFactoryService,
   private val auditScheduleScheduleFactoryService: AuditScheduleFactoryService,
   private val companyRepository: CompanyRepository,
   private val employeeFactoryService: EmployeeFactoryService,
   private val divisionFactoryService: DivisionFactoryService,
   private val regionFactoryService: RegionFactoryService,
   private val storeFactoryService: StoreFactoryService
) {
   private val logger: Logger = LoggerFactory.getLogger(DevelopDataLoader::class.java)

   fun loadDemoData() {
      logger.info("Loading develop data")

      val companies = CompanyFactory.predefined().asSequence()
         .map { company ->
            when(company.datasetCode) {
               "tstds1" -> company.copy(datasetCode = "corrto")
               "tstds2" -> company.copy(datasetCode = "corptp")
               else -> company
            }
         }
         .map { companyRepository.insert(it) }
         .toList()
      val companyTstds1 = companies.first { it.datasetCode == "corrto" }
      val companyTstds2 = companies.first { it.datasetCode == "corptp" }

      val divisionOne = divisionFactoryService.single(companyTstds1)
      val divisionTwo = divisionFactoryService.single(companyTstds1)

      val regionOne = regionFactoryService.single(divisionOne)
      val regionTwo = regionFactoryService.single(divisionOne)
      val regionThree = regionFactoryService.single(divisionTwo)
      val regionFour = regionFactoryService.single(divisionTwo)

      val storeOneCompanyOne = storeFactoryService.store(1, companyTstds1)
      val storeThreeCompanyOne = storeFactoryService.store(3, companyTstds1)

      val storeTwoCompanyTwo = storeFactoryService.store(2, companyTstds2)
      val storeFourCompanyTwo = storeFactoryService.store(4, companyTstds2)
      val storeFiveCompanyTwo = storeFactoryService.store(5, companyTstds2)

      storeFactoryService.createRegionToStore(storeOneCompanyOne, regionOne)
      storeFactoryService.createRegionToStore(storeThreeCompanyOne, regionTwo)

      storeFactoryService.createRegionToStore(storeTwoCompanyTwo, regionThree)
      storeFactoryService.createRegionToStore(storeFourCompanyTwo, regionFour)
      storeFactoryService.createRegionToStore(storeFiveCompanyTwo, regionFour)

      val storeOneEmployee = employeeFactoryService.single(storeIn = storeOneCompanyOne)
      val storeThreeEmployee = employeeFactoryService.single(storeIn = storeOneCompanyOne)

      // audit store holding areas
      val warehouse = auditScanAreaFactoryService.warehouse()
      val showroom = auditScanAreaFactoryService.showroom()
      val storeroom = auditScanAreaFactoryService.storeroom()

      // setup store one open audit
      val openStoreOneAudit = auditFactoryService.single(storeOneEmployee, statusesIn = setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()))
      auditDetailFactoryService.generate(11, openStoreOneAudit, storeOneEmployee, warehouse)
      auditDetailFactoryService.generate(5, openStoreOneAudit, storeOneEmployee, showroom)
      auditDetailFactoryService.generate(5, openStoreOneAudit, storeOneEmployee, storeroom)
      auditExceptionFactoryService.generate(25, openStoreOneAudit, storeOneEmployee)

      // setup store three open audit
      val openStoreThreeAudit = auditFactoryService.single(storeThreeEmployee.store!!)
      auditDetailFactoryService.generate(9, openStoreThreeAudit, storeThreeEmployee, warehouse)
      auditDetailFactoryService.generate(5, openStoreThreeAudit, storeThreeEmployee, showroom)
      auditDetailFactoryService.generate(5, openStoreThreeAudit, storeThreeEmployee, storeroom)
      auditExceptionFactoryService.generate(26, openStoreThreeAudit, storeThreeEmployee)

      // setup store one canceled audit
      auditFactoryService.single(storeOneEmployee, setOf(AuditStatusFactory.created(), AuditStatusFactory.canceled()))

      // setup store three canceled audit
      auditFactoryService.single(storeThreeEmployee, setOf(AuditStatusFactory.created(), AuditStatusFactory.canceled()))

      // setup store three in-progress audit
      auditFactoryService.single(storeThreeEmployee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress()))

      // setup store one completed off audits
      auditFactoryService.generate(3, storeOneEmployee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()))

      // setup store three completed off audits
      auditFactoryService.generate(4, storeThreeEmployee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()))

      // setup store one signed off audits
      auditFactoryService.generate(3, storeOneEmployee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.signedOff()))

      // setup store three signed off audits
      auditFactoryService.generate(4, storeThreeEmployee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.signedOff()))

      auditScheduleScheduleFactoryService.single(TUESDAY, listOf(storeOneCompanyOne), AuthenticatedEmployee(storeOneEmployee.id!!, storeOneEmployee, storeOneCompanyOne), companyTstds1)
      auditScheduleScheduleFactoryService.single(THURSDAY, listOf(storeThreeCompanyOne), AuthenticatedEmployee(storeThreeEmployee.id!!, storeThreeEmployee, storeThreeCompanyOne), companyTstds1)

      logger.info("Finished loading develop data")
      logger.info("Store one employee {}", storeOneEmployee)
      logger.info("Store three employee {}", storeThreeEmployee)
   }
}
