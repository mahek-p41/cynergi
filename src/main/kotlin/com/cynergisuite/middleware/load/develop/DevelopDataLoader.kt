package com.cynergisuite.middleware.load.develop

import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.detail.AuditDetailFactoryService
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactoryService
import com.cynergisuite.middleware.audit.exception.AuditExceptionFactoryService
import com.cynergisuite.middleware.audit.schedule.AuditScheduleFactoryService
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.department.DepartmentFactoryService
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.load.legacy.LegacyLoadFinishedEvent
import com.cynergisuite.middleware.store.StoreFactoryService
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.ApplicationEventListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.DayOfWeek
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
   private val departmentFactoryService: DepartmentFactoryService,
   private val employeeRepository: EmployeeRepository,
   private val employeeFactoryService: EmployeeFactoryService,
   private val storeFactoryService: StoreFactoryService
) : ApplicationEventListener<LegacyLoadFinishedEvent> {
   private val logger: Logger = LoggerFactory.getLogger(DevelopDataLoader::class.java)

   override fun onApplicationEvent(event: LegacyLoadFinishedEvent?) {
      logger.info("Loading develop data")

      val storeOne = storeFactoryService.store(1)
      val storeThree = storeFactoryService.store(3)
      val admin = employeeRepository.findOne(998, "int")
      val storeOneEmployee = employeeFactoryService.single(storeOne)
      val storeThreeEmployee = employeeFactoryService.single(storeThree)
      val salesAssociateDepartment = departmentFactoryService.department("SA")

      // audit store holding areas
      val warehouse = auditScanAreaFactoryService.warehouse()
      val showroom = auditScanAreaFactoryService.showroom()
      val storeroom = auditScanAreaFactoryService.storeroom()

      // setup store one open audit
      val openStoreOneAudit = auditFactoryService.single(storeOne, storeOneEmployee)
      auditDetailFactoryService.generate(11, openStoreOneAudit, storeOneEmployee, warehouse)
      auditDetailFactoryService.generate(5, openStoreOneAudit, storeOneEmployee, showroom)
      auditDetailFactoryService.generate(5, openStoreOneAudit, storeOneEmployee, storeroom)
      auditExceptionFactoryService.generate(25, openStoreOneAudit, storeOneEmployee)

      // setup store three open audit
      val openStoreThreeAudit = auditFactoryService.single(storeThree, storeThreeEmployee)
      auditDetailFactoryService.generate(9, openStoreThreeAudit, storeThreeEmployee, warehouse)
      auditDetailFactoryService.generate(5, openStoreThreeAudit, storeThreeEmployee, showroom)
      auditDetailFactoryService.generate(5, openStoreThreeAudit, storeThreeEmployee, storeroom)
      auditExceptionFactoryService.generate(26, openStoreThreeAudit, storeThreeEmployee)

      // setup store one canceled audit
      auditFactoryService.single(storeOne, storeOneEmployee, setOf(AuditStatusFactory.created(), AuditStatusFactory.canceled()))

      // setup store three canceled audit
      auditFactoryService.single(storeThree, storeThreeEmployee, setOf(AuditStatusFactory.created(), AuditStatusFactory.canceled()))

      // setup store three in-progress audit
      auditFactoryService.single(storeThree, storeThreeEmployee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress()))

      // setup store one completed off audits
      auditFactoryService.generate(3, storeOne, storeOneEmployee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()))

      // setup store three completed off audits
      auditFactoryService.generate(4, storeThree, storeThreeEmployee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()))

      // setup store one signed off audits
      auditFactoryService.generate(3, storeOne, storeOneEmployee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.signedOff()))

      // setup store three signed off audits
      auditFactoryService.generate(4, storeThree, storeThreeEmployee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.signedOff()))

      auditScheduleScheduleFactoryService.single(DayOfWeek.TUESDAY, listOf(storeOne), salesAssociateDepartment)
      auditScheduleScheduleFactoryService.single(DayOfWeek.THURSDAY, listOf(storeThree), salesAssociateDepartment)

      logger.info("Finished loading develop data")
      logger.info("Admin employee {}", admin)
      logger.info("Store one employee {}", storeOneEmployee)
      logger.info("Store three employee {}", storeThreeEmployee)
   }
}
