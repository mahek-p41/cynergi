package com.cynergisuite.middleware.load.develop

import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.detail.AuditDetailFactoryService
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaFactoryService
import com.cynergisuite.middleware.audit.exception.AuditExceptionFactoryService
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNoteFactoryService
import com.cynergisuite.middleware.audit.schedule.AuditScheduleFactoryService
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.DepartmentFactoryService
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
import kotlin.random.Random

@Singleton
@Requires(env = ["develop"])
class DevelopDataLoader @Inject constructor(
   private val auditDetailFactoryService: AuditDetailFactoryService,
   private val auditExceptionFactoryService: AuditExceptionFactoryService,
   private val auditExceptionNoteFactoryService: AuditExceptionNoteFactoryService,
   private val auditFactoryService: AuditFactoryService,
   private val auditScanAreaFactoryService: AuditScanAreaFactoryService,
   private val auditScheduleScheduleFactoryService: AuditScheduleFactoryService,
   private val companyRepository: CompanyRepository,
   private val departmentFactoryService: DepartmentFactoryService,
   private val divisionFactoryService: DivisionFactoryService,
   private val employeeFactoryService: EmployeeFactoryService,
   private val regionFactoryService: RegionFactoryService,
   private val storeFactoryService: StoreFactoryService
) {
   private val logger: Logger = LoggerFactory.getLogger(DevelopDataLoader::class.java)

   fun loadDemoData() {
      logger.info("Loading develop data")

      val companies = CompanyFactory.predefinedDevData().map { companyRepository.insert(it) }.toList()

      // begin setting up corrto stores and employees
      val corrto = companies.first { it.datasetCode == "corrto" }
      val corrtoStoreManagerDepartment = departmentFactoryService.department("SM", corrto)
      val corrtoDivision1 = divisionFactoryService.single(corrto)
      val corrtoDivison1Region1 = regionFactoryService.single(corrtoDivision1)
      val corrtoStore1 = storeFactoryService.store(1, corrto)
      val corrtoStore3 = storeFactoryService.store(3, corrto)
      storeFactoryService.companyStoresToRegion(corrtoDivison1Region1, corrtoStore1, corrtoStore3)

      val nineNineEightEmployeeCorrtoEmployee = employeeFactoryService.single(998, corrto, "user", "super", "pass", true, "A", 0)
      val corrtoStore1StoreManager = employeeFactoryService.single(storeIn = corrtoStore1, departmentIn = corrtoStoreManagerDepartment)
      val corrtoStore3StoreManager = employeeFactoryService.single(storeIn = corrtoStore3, departmentIn = corrtoStoreManagerDepartment)
      // end setting up corrto stores and employees

      // begin setting up corptp stores and employees
      val corptp = companies.first { it.datasetCode == "corptp" }
      val corptpStoreManagerDepartment = departmentFactoryService.department("SM", corptp)
      val corptpStore1 = storeFactoryService.store(1, corptp)
      val corptpDivison1 = divisionFactoryService.single(corptp)
      val corptpDivison1Region1 = regionFactoryService.single(corptpDivison1)
      storeFactoryService.companyStoresToRegion(corptpDivison1Region1, corptpStore1)

      val corptpStore2 = storeFactoryService.store(2, corptp)
      val corptpDivsion1Region2 = regionFactoryService.single(corptpDivison1)
      storeFactoryService.companyStoresToRegion(corptpDivsion1Region2, corptpStore2)

      val corptpDivsion2 = divisionFactoryService.single(corptp)
      val corptpDivison2Region1 = regionFactoryService.single(corptpDivsion2)
      val corptpStore3 = storeFactoryService.store(3, corptp)
      val corptpStore4 = storeFactoryService.store(4, corptp)
      val corptpStore5 = storeFactoryService.store(5, corptp)
      storeFactoryService.companyStoresToRegion(corptpDivison2Region1, corptpStore3, corptpStore4, corptpStore5)

      val nineNineEightEmployeeCorptpEmployee = employeeFactoryService.single(998, corptp, "user", "super", "pass", true, "A", 0)
      val corptpStore1StoreManager = employeeFactoryService.single(corptpStore1, corptpStoreManagerDepartment)
      val corptpStore2StoreManager = employeeFactoryService.single(corptpStore2, corptpStoreManagerDepartment)
      val corptpStore3StoreManager = employeeFactoryService.single(corptpStore3, corptpStoreManagerDepartment)
      val corptpStore4StoreManager = employeeFactoryService.single(corptpStore4, corptpStoreManagerDepartment)
      val corptpStore5StoreManager = employeeFactoryService.single(corptpStore5, corptpStoreManagerDepartment)
      // end setting up corptp stores and employees

      // audit store holding areas
      val warehouse = auditScanAreaFactoryService.warehouse()
      val showroom = auditScanAreaFactoryService.showroom()
      val storeroom = auditScanAreaFactoryService.storeroom()

      // setup store one completed audit
      val completedStore1Audit = auditFactoryService.single(corrtoStore1StoreManager, statusesIn = setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()))
      // Audit detail count 21
      auditDetailFactoryService.generate(11, completedStore1Audit, corrtoStore1StoreManager, warehouse)
      auditDetailFactoryService.generate(5, completedStore1Audit, corrtoStore1StoreManager, showroom)
      auditDetailFactoryService.generate(5, completedStore1Audit, corrtoStore1StoreManager, storeroom)
      // create random [0..2] notes for each audit exception
      auditExceptionFactoryService.generate(25, completedStore1Audit, corrtoStore1StoreManager).forEach {
         auditExceptionNoteFactoryService.stream(Random.nextInt(2), it, corrtoStore1StoreManager).forEach { }
      }

      // setup store three open audit
      val openStore3Audit = auditFactoryService.single(corrtoStore3StoreManager.store!!)
      auditDetailFactoryService.generate(9, openStore3Audit, corrtoStore3StoreManager, warehouse)
      auditDetailFactoryService.generate(5, openStore3Audit, corrtoStore3StoreManager, showroom)
      auditDetailFactoryService.generate(5, openStore3Audit, corrtoStore3StoreManager, storeroom)
      // create random [0..2] notes for each audit exception
      auditExceptionFactoryService.generate(26, openStore3Audit, corrtoStore3StoreManager).forEach {
         auditExceptionNoteFactoryService.stream(Random.nextInt(2), it, corrtoStore3StoreManager).forEach {}
      }
      val auditException =  auditExceptionFactoryService.single(openStore3Audit, corrtoStore3StoreManager, true)
      auditExceptionNoteFactoryService.stream(Random.nextInt(2), auditException, corrtoStore3StoreManager).forEach { }

      // setup store one canceled audit
      auditFactoryService.single(corrtoStore1StoreManager, setOf(AuditStatusFactory.created(), AuditStatusFactory.canceled()))

      // setup store three canceled audit
      auditFactoryService.single(corrtoStore3StoreManager, setOf(AuditStatusFactory.created(), AuditStatusFactory.canceled()))

      // setup store three in-progress audit
      auditFactoryService.single(corrtoStore3StoreManager, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress()))

      // setup store one completed off audits
      auditFactoryService.generate(3, corrtoStore1StoreManager, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()))

      // setup store three completed off audits
      auditFactoryService.generate(4, corrtoStore3StoreManager, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()))

      // setup store one signed off audits
      auditFactoryService.generate(3, corrtoStore1StoreManager, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.signedOff()))

      // setup store three signed off audits
      auditFactoryService.generate(4, corrtoStore3StoreManager, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.signedOff()))

      auditScheduleScheduleFactoryService.single(TUESDAY, listOf(corrtoStore1), AuthenticatedEmployee(corrtoStore1StoreManager.id!!, corrtoStore1StoreManager, corrtoStore1), corrto)
      auditScheduleScheduleFactoryService.single(THURSDAY, listOf(corrtoStore3), AuthenticatedEmployee(corrtoStore3StoreManager.id!!, corrtoStore3StoreManager, corrtoStore3), corrto)

      logger.info("Finished loading develop data")
      logger.info("Store 1 corrto employee {} / {} -> Store Number {} -> Department {}", corrtoStore1StoreManager.number, corrtoStore1StoreManager.passCode, corrtoStore1StoreManager.store?.myNumber(), corrtoStore1StoreManager.department?.myCode())
      logger.info("Store 3 corrto employee {} / {} -> Store Number {} -> Department {}", corrtoStore3StoreManager.number, corrtoStore3StoreManager.passCode, corrtoStore3StoreManager.store.myNumber(), corrtoStore3StoreManager.department?.myCode())
      logger.info("Corrto 998 User {} / {}", nineNineEightEmployeeCorrtoEmployee.number, nineNineEightEmployeeCorrtoEmployee.passCode)

      logger.info("Store 1 corptp employee {} / {} -> Store Number {} -> Department {}", corptpStore1StoreManager.number, corptpStore1StoreManager.passCode, corptpStore1StoreManager.store?.myNumber(), corptpStore1StoreManager.department?.myCode())
      logger.info("Store 2 corptp employee {} / {} -> Store Number {} -> Department {}", corptpStore2StoreManager.number, corptpStore2StoreManager.passCode, corptpStore2StoreManager.store?.myNumber(), corptpStore2StoreManager.department?.myCode())
      logger.info("Store 3 corptp employee {} / {} -> Store Number {} -> Department {}", corptpStore3StoreManager.number, corptpStore3StoreManager.passCode, corptpStore3StoreManager.store?.myNumber(), corptpStore3StoreManager.department?.myCode())
      logger.info("Store 4 corptp employee {} / {} -> Store Number {} -> Department {}", corptpStore4StoreManager.number, corptpStore4StoreManager.passCode, corptpStore4StoreManager.store?.myNumber(), corptpStore4StoreManager.department?.myCode())
      logger.info("Store 5 corptp employee {} / {} -> Store Number {} -> Department {}", corptpStore5StoreManager.number, corptpStore5StoreManager.passCode, corptpStore5StoreManager.store?.myNumber(), corptpStore5StoreManager.department?.myCode())
      logger.info("Corptp 998 User {} / {}", nineNineEightEmployeeCorptpEmployee.number, nineNineEightEmployeeCorptpEmployee.passCode)
   }
}
