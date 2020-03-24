package com.cynergisuite.middleware.load.develop

import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.detail.AuditDetailFactoryService
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactoryService
import com.cynergisuite.middleware.audit.exception.AuditExceptionFactoryService
import com.cynergisuite.middleware.audit.schedule.AuditScheduleFactoryService
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.company.Company
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
import kotlin.streams.toList

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

      val companies = CompanyFactory.predefinedDevData().asSequence()
         .map { companyRepository.insert(it) }
         .toList()
      val companyCorrto = companies.first { it.datasetCode == "corrto" }

      val divisions = companies.map { company ->  divisionFactoryService.single(company) }.toList()
      val regions = divisions.map { division -> regionFactoryService.single(division) }.toList()
      regions.map { region -> storeFactoryService.companyStoresToRegionWithDevData(region.division.company as Company, region).toList() }

      val store1Corrto = storeFactoryService.store(1, companyCorrto)
      val store3Corrto = storeFactoryService.store(3, companyCorrto)

      val store1Employee = employeeFactoryService.single(storeIn = store1Corrto)
      val store3Employee = employeeFactoryService.single(storeIn = store3Corrto)

      // audit store holding areas
      val warehouse = auditScanAreaFactoryService.warehouse()
      val showroom = auditScanAreaFactoryService.showroom()
      val storeroom = auditScanAreaFactoryService.storeroom()

      // setup store one open audit
      val openStore1Audit = auditFactoryService.single(store1Employee, statusesIn = setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()))
      auditDetailFactoryService.generate(11, openStore1Audit, store1Employee, warehouse)
      auditDetailFactoryService.generate(5, openStore1Audit, store1Employee, showroom)
      auditDetailFactoryService.generate(5, openStore1Audit, store1Employee, storeroom)
      auditExceptionFactoryService.generate(25, openStore1Audit, store1Employee)

      // setup store three open audit
      val openStore3Audit = auditFactoryService.single(store3Employee.store!!)
      auditDetailFactoryService.generate(9, openStore3Audit, store3Employee, warehouse)
      auditDetailFactoryService.generate(5, openStore3Audit, store3Employee, showroom)
      auditDetailFactoryService.generate(5, openStore3Audit, store3Employee, storeroom)
      auditExceptionFactoryService.generate(26, openStore3Audit, store3Employee)

      // setup store one canceled audit
      auditFactoryService.single(store1Employee, setOf(AuditStatusFactory.created(), AuditStatusFactory.canceled()))

      // setup store three canceled audit
      auditFactoryService.single(store3Employee, setOf(AuditStatusFactory.created(), AuditStatusFactory.canceled()))

      // setup store three in-progress audit
      auditFactoryService.single(store3Employee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress()))

      // setup store one completed off audits
      auditFactoryService.generate(3, store1Employee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()))

      // setup store three completed off audits
      auditFactoryService.generate(4, store3Employee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()))

      // setup store one signed off audits
      auditFactoryService.generate(3, store1Employee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.signedOff()))

      // setup store three signed off audits
      auditFactoryService.generate(4, store3Employee, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.signedOff()))

      auditScheduleScheduleFactoryService.single(TUESDAY, listOf(store1Corrto), AuthenticatedEmployee(store1Employee.id!!, store1Employee, store1Corrto), companyCorrto)
      auditScheduleScheduleFactoryService.single(THURSDAY, listOf(store3Corrto), AuthenticatedEmployee(store3Employee.id!!, store3Employee, store3Corrto), companyCorrto)

      logger.info("Finished loading develop data")
      logger.info("Store one employee {}", store1Employee)
      logger.info("Store three employee {}", store3Employee)
   }
}
