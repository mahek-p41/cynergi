package com.cynergisuite.middleware.load.demo

import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.detail.AuditDetailFactoryService
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactoryService
import com.cynergisuite.middleware.audit.exception.AuditExceptionFactoryService
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.load.legacy.LegacyLoadFinishedEvent
import com.cynergisuite.middleware.store.StoreFactoryService
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.ApplicationEventListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Requires(env = ["demo"])
class DemoDataLoader @Inject constructor(
   private val auditDetailFactoryService: AuditDetailFactoryService,
   private val auditDiscrepancyFactoryService: AuditExceptionFactoryService,
   private val auditFactoryService: AuditFactoryService,
   private val auditScanAreaFactoryService: AuditScanAreaFactoryService,
   private val employeeRepository: EmployeeRepository,
   private val storeFactoryService: StoreFactoryService
) : ApplicationEventListener<LegacyLoadFinishedEvent> {
   private val logger: Logger = LoggerFactory.getLogger(DemoDataLoader::class.java)

   override fun onApplicationEvent(event: LegacyLoadFinishedEvent?) {
      logger.info("Loading demo data")

      val store = storeFactoryService.store(1)
      val audit = auditFactoryService.single(store)
      val employee = employeeRepository.findOne(998, "int")
      val warehouse = auditScanAreaFactoryService.warehouse()
      val showroom = auditScanAreaFactoryService.showroom()
      val storeroom = auditScanAreaFactoryService.storeroom()

      auditDetailFactoryService.stream(11, audit, employee, warehouse).forEach { logger.debug("Loaded audit detail {}", it) }
      auditDetailFactoryService.stream(5, audit, employee, showroom).forEach { logger.debug("Loaded audit detail {}", it) }
      auditDetailFactoryService.stream(5, audit, employee, storeroom).forEach { logger.debug("Loaded audit detail {}", it) }

      auditDiscrepancyFactoryService.stream(25, audit, employee).forEach { logger.debug("Loaded audit discrepancy {}", it) }

      logger.info("Finished loading demo data")
   }
}
