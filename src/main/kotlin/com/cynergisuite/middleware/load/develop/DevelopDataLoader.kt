package com.cynergisuite.middleware.load.develop

import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.detail.AuditDetailFactoryService
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactoryService
import com.cynergisuite.middleware.audit.exception.AuditExceptionFactoryService
import com.cynergisuite.middleware.audit.schedule.AuditScheduleFactoryService
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.store.StoreFactoryService
import io.micronaut.context.annotation.Requires
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
      val storeOne = storeFactoryService.store(1, companyTstds1)
      val storeThree = storeFactoryService.store(3, companyTstds1)
      val storeOneEmployee = employeeFactoryService.single(storeIn = storeOne)
      val storeThreeEmployee = employeeFactoryService.single(storeIn = storeThree)

      // audit store holding areas
      val warehouse = auditScanAreaFactoryService.warehouse()
      val showroom = auditScanAreaFactoryService.showroom()
      val storeroom = auditScanAreaFactoryService.storeroom()

      // setup store one open audit
      val openStoreOneAudit = auditFactoryService.single(storeOneEmployee, statusesIn = setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()))
      auditDetailFactoryService.generate(11, openStoreOneAudit, storeOneEmployee, warehouse)
      auditDetailFactoryService.generate(5, openStoreOneAudit, storeOneEmployee, showroom)
      auditDetailFactoryService.generate(5, openStoreOneAudit, storeOneEmployee, storeroom)
      //auditExceptionFactoryService.generate(25, openStoreOneAudit, storeOneEmployee)

      // setup store three open audit
      val openStoreThreeAudit = auditFactoryService.single(storeThreeEmployee.store!!)
      auditDetailFactoryService.generate(9, openStoreThreeAudit, storeThreeEmployee, warehouse)
      auditDetailFactoryService.generate(5, openStoreThreeAudit, storeThreeEmployee, showroom)
      auditDetailFactoryService.generate(5, openStoreThreeAudit, storeThreeEmployee, storeroom)
      //auditExceptionFactoryService.generate(26, openStoreThreeAudit, storeThreeEmployee)

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

      //auditScheduleScheduleFactoryService.single(DayOfWeek.TUESDAY, listOf(storeOne))
      //auditScheduleScheduleFactoryService.single(DayOfWeek.THURSDAY, listOf(storeThree))

      logger.info("Finished loading develop data")
      logger.info("Store one employee {}", storeOneEmployee)
      logger.info("Store three employee {}", storeThreeEmployee)
   }
}
