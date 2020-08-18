package com.cynergisuite.middleware.load.develop

import com.cynergisuite.middleware.accounting.account.AccountDataLoaderService
import com.cynergisuite.middleware.accounting.bank.BankFactoryService
import com.cynergisuite.middleware.area.AreaDataLoaderService
import com.cynergisuite.middleware.area.ModuleDataLoaderService
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
import com.cynergisuite.middleware.shipping.shipvia.ShipViaTestDataLoaderService
import com.cynergisuite.middleware.store.StoreFactoryService
import com.cynergisuite.middleware.vendor.VendorTestDataLoaderService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermTestDataLoaderService
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
   private val storeFactoryService: StoreFactoryService,
   private val accountDataLoaderService: AccountDataLoaderService,
   private val bankFactoryService: BankFactoryService,
   private val areaDataLoaderService: AreaDataLoaderService,
   private val moduleDataLoaderService: ModuleDataLoaderService,
   private val shipViaDataLoaderService: ShipViaTestDataLoaderService,
   private val vendorPaymentTermDataLoaderService: VendorPaymentTermTestDataLoaderService,
   private val vendorDataLoaderService: VendorTestDataLoaderService
) {
   private val logger: Logger = LoggerFactory.getLogger(DevelopDataLoader::class.java)

   fun loadDemoData() {
      logger.info("Loading develop data")

      val companies = CompanyFactory.predefinedDevData().map { companyRepository.insert(it) }.toList()

      // begin setting up corrto stores and employees
      val corrto = companies.first { it.datasetCode == "corrto" }
      val corrtoStoreManagerDepartment = departmentFactoryService.department("SM", corrto)
      val corrtoRegionalManagerDepartment = departmentFactoryService.department("RM", corrto)
      val corrtoDivisionalManagerDepartment = departmentFactoryService.department("EX", corrto)
      val corrtoStore1 = storeFactoryService.store(1, corrto)
      val corrtoStore1StoreManager = employeeFactoryService.single(storeIn = corrtoStore1, departmentIn = corrtoStoreManagerDepartment)
      val corrtoStore1DivisionalManager = employeeFactoryService.single(departmentIn = corrtoDivisionalManagerDepartment)
      val corrtoDivision1 = divisionFactoryService.single(corrto, corrtoStore1DivisionalManager)

      val corrtoDivision1Region1 = regionFactoryService.single(corrtoDivision1)
      regionFactoryService.single(corrtoDivision1, corrtoStore1StoreManager)
      val corrtoStore3 = storeFactoryService.store(3, corrto)
      val corrtoStoreHomeOffice = storeFactoryService.store(9000, corrto)
      storeFactoryService.companyStoresToRegion(corrtoDivision1Region1, corrtoStore1, corrtoStore3)

      val nineNineEightEmployeeCorrtoEmployee = employeeFactoryService.singleSuperUser(998, corrto, "user", "super", "pass")
      val corrtoStore3StoreManager = employeeFactoryService.single(corrtoStore3, corrtoStoreManagerDepartment)
      val corrtoRegion1Manager = employeeFactoryService.single(1000, corrto, corrtoRegionalManagerDepartment, corrtoStoreHomeOffice, "manager", "regional", "12345", "R", corrtoDivision1Region1.id!!)
      // end setting up corrto stores and employees

      // begin setting up corptp stores and employees
      val corptp = companies.first { it.datasetCode == "corptp" }
      val corptpStoreManagerDepartment = departmentFactoryService.department("SM", corptp)
      storeFactoryService.store(9000, corptp)
      val corptpStore1 = storeFactoryService.store(1, corptp)
      val corptpStore1StoreManager = employeeFactoryService.single(corptpStore1, corptpStoreManagerDepartment)

      val corptpDivision2 = divisionFactoryService.single(corptp, corptpStore1StoreManager)
      val corptpDivision2Region2 = regionFactoryService.single(corptpDivision2)
      storeFactoryService.companyStoresToRegion(corptpDivision2Region2, corptpStore1)

      val corptpStore2 = storeFactoryService.store(2, corptp)
      val corptpDivision2Region3 = regionFactoryService.single(corptpDivision2)
      storeFactoryService.companyStoresToRegion(corptpDivision2Region3, corptpStore2)

      val corptpDivision3 = divisionFactoryService.single(corptp, corptpStore1StoreManager)
      val corptpDivision3Region4 = regionFactoryService.single(corptpDivision3)
      val corptpStore3 = storeFactoryService.store(3, corptp)
      val corptpStore4 = storeFactoryService.store(4, corptp)
      val corptpStore5 = storeFactoryService.store(5, corptp)
      storeFactoryService.companyStoresToRegion(corptpDivision3Region4, corptpStore3, corptpStore4, corptpStore5)

      val nineNineEightEmployeeCorptpEmployee = employeeFactoryService.singleSuperUser(998, corptp, "user", "super", "pass")
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
      auditExceptionFactoryService.stream(25, completedStore1Audit, corrtoStore1StoreManager).forEach {
         auditExceptionNoteFactoryService.stream(Random.nextInt(2), it, corrtoStore1StoreManager).forEach { }
      }

      // setup store three open audit
      val openStore3Audit = auditFactoryService.single(corrtoStore3StoreManager.store!!)
      auditDetailFactoryService.generate(9, openStore3Audit, corrtoStore3StoreManager, warehouse)
      auditDetailFactoryService.generate(5, openStore3Audit, corrtoStore3StoreManager, showroom)
      auditDetailFactoryService.generate(5, openStore3Audit, corrtoStore3StoreManager, storeroom)
      // create random [0..2] notes for each audit exception
      auditExceptionFactoryService.stream(26, openStore3Audit, corrtoStore3StoreManager).forEach {
         auditExceptionNoteFactoryService.stream(Random.nextInt(2), it, corrtoStore3StoreManager).forEach {}
      }
      val auditException = auditExceptionFactoryService.single(openStore3Audit, corrtoStore3StoreManager, true)
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

      // setup store one approved audits
      auditFactoryService.generate(3, corrtoStore1StoreManager, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.approved()))

      // setup store three approved audits
      auditFactoryService.generate(4, corrtoStore3StoreManager, setOf(AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed(), AuditStatusFactory.approved()))

      auditScheduleScheduleFactoryService.single(TUESDAY, listOf(corrtoStore1), AuthenticatedEmployee(corrtoStore1StoreManager.id!!, corrtoStore1StoreManager, corrtoStore1), corrto)
      auditScheduleScheduleFactoryService.single(THURSDAY, listOf(corrtoStore3), AuthenticatedEmployee(corrtoStore3StoreManager.id!!, corrtoStore3StoreManager, corrtoStore3), corrto)

      // setup account & bank
      val corrtoAccount = accountDataLoaderService.single(corrto)
      bankFactoryService.stream(3, corrto, corrtoStore1, corrtoAccount).forEach { }
      bankFactoryService.stream(3, corrto, corrtoStore3, corrtoAccount).forEach { }

      val corptpAccount = accountDataLoaderService.single(corptp)
      bankFactoryService.stream(3, corptp, corptpStore1, corptpAccount).forEach { }
      bankFactoryService.stream(3, corptp, corptpStore2, corptpAccount).forEach { }

      areaDataLoaderService.enableArea(1, companies[0])
      areaDataLoaderService.enableArea(5, companies[0])
      areaDataLoaderService.enableArea(5, companies[1])

      moduleDataLoaderService.configureLevel(68, 90, companies[0])
      moduleDataLoaderService.configureLevel(69, 80, companies[0])
      moduleDataLoaderService.configureLevel(71, 20, companies[0])
      moduleDataLoaderService.configureLevel(72, 10, companies[0])
      moduleDataLoaderService.configureLevel(68, 80, companies[1])
      moduleDataLoaderService.configureLevel(69, 70, companies[1])
      moduleDataLoaderService.configureLevel(71, 50, companies[1])
      moduleDataLoaderService.configureLevel(72, 40, companies[1])

      val shipVia = shipViaDataLoaderService.single(corrto)
      val vendorPaymentTerm = vendorPaymentTermDataLoaderService.singleWithSingle90DaysPayment(corrto)
      vendorDataLoaderService.stream(3, corrto, vendorPaymentTerm, shipVia).forEach {}

      logger.info("Finished loading develop data")
      logger.info("Store 1 corrto employee {} / {} -> Store Number {} -> Department {}", corrtoStore1StoreManager.number, corrtoStore1StoreManager.passCode, corrtoStore1StoreManager.store?.myNumber(), corrtoStore1StoreManager.department?.myCode())
      logger.info("Store 3 corrto employee {} / {} -> Store Number {} -> Department {}", corrtoStore3StoreManager.number, corrtoStore3StoreManager.passCode, corrtoStore3StoreManager.store.myNumber(), corrtoStore3StoreManager.department?.myCode())
      logger.info("Division {} Region {} regional manager {} / {} -> ", corrtoDivision1Region1.division.number, corrtoDivision1Region1.id, corrtoRegion1Manager.number, corrtoRegion1Manager.passCode)
      logger.info("Corrto 998 User {} / {}", nineNineEightEmployeeCorrtoEmployee.number, nineNineEightEmployeeCorrtoEmployee.passCode)

      logger.info("Store 1 corptp employee {} / {} -> Store Number {} -> Department {}", corptpStore1StoreManager.number, corptpStore1StoreManager.passCode, corptpStore1StoreManager.store?.myNumber(), corptpStore1StoreManager.department?.myCode())
      logger.info("Store 2 corptp employee {} / {} -> Store Number {} -> Department {}", corptpStore2StoreManager.number, corptpStore2StoreManager.passCode, corptpStore2StoreManager.store?.myNumber(), corptpStore2StoreManager.department?.myCode())
      logger.info("Store 3 corptp employee {} / {} -> Store Number {} -> Department {}", corptpStore3StoreManager.number, corptpStore3StoreManager.passCode, corptpStore3StoreManager.store?.myNumber(), corptpStore3StoreManager.department?.myCode())
      logger.info("Store 4 corptp employee {} / {} -> Store Number {} -> Department {}", corptpStore4StoreManager.number, corptpStore4StoreManager.passCode, corptpStore4StoreManager.store?.myNumber(), corptpStore4StoreManager.department?.myCode())
      logger.info("Store 5 corptp employee {} / {} -> Store Number {} -> Department {}", corptpStore5StoreManager.number, corptpStore5StoreManager.passCode, corptpStore5StoreManager.store?.myNumber(), corptpStore5StoreManager.department?.myCode())
      logger.info("Corptp 998 User {} / {}", nineNineEightEmployeeCorptpEmployee.number, nineNineEightEmployeeCorptpEmployee.passCode)
   }
}
