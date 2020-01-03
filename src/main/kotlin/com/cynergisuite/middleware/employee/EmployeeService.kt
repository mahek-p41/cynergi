package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.CSVParsingService
import com.cynergisuite.extensions.isDigits
import com.cynergisuite.extensions.trimToNull
import com.cynergisuite.middleware.authentication.User
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.load.legacy.LegacyCsvLoadingService
import com.cynergisuite.middleware.store.StoreService
import io.micronaut.validation.Validated
import io.reactivex.Maybe
import org.apache.commons.csv.CSVRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class EmployeeService @Inject constructor(
   private val employeeRepository: EmployeeRepository,
   private val employeeValidator: EmployeeValidator,
   private val storeService: StoreService
) : CSVParsingService(), LegacyCsvLoadingService {
   private val logger: Logger = LoggerFactory.getLogger(EmployeeService::class.java)
   private val employeeMatcher = FileSystems.getDefault().getPathMatcher("glob:eli-employee*csv")

   fun exists(id: Long, employeeType: String, dataset: String): Boolean =
      employeeRepository.exists(id = id, employeeType = employeeType, dataset = dataset)

   @Validated
   fun create(@Valid vo: EmployeeValueObject): EmployeeValueObject {
      employeeValidator.validateCreate(vo)

      return EmployeeValueObject(
         entity = employeeRepository.insert(entity = EmployeeEntity(vo = vo))
      )
   }

   fun canEmployeeAccess(asset: String, employee: User): Boolean {
      logger.debug("Checking if the user {} has access to asset {}", employee, asset)

      return employeeRepository.canEmployeeAccess(employee.myEmployeeType(), asset, employee.myId() ?: -1) // user -1 since that shouldn't be a valid id in the system, and will cause the access check to fail
   }

   fun fetchUserByAuthentication(number: Int, passCode: String, dataset: String, storeNumber: Int? = null): Maybe<EmployeeEntity> =
      employeeRepository.findUserByAuthentication(number, passCode, dataset, storeNumber)

   override fun canProcess(path: Path): Boolean =
      employeeMatcher.matches(path.fileName)

   override fun processCsvRow(record: CSVRecord) {
      val storeNumberIn = record.get("store_number").trimToNull()
      val dataset = record.get("dataset").trimToNull()

      if ( (storeNumberIn.isNullOrBlank() || storeNumberIn.isDigits()) && dataset != null ) {
         val store = storeNumberIn?.let { storeService.fetchByNumber(it.toInt(), dataset) }

         create (
            EmployeeValueObject(
               type = "eli",
               number = record.get("number").toInt(),
               dataset = record.get("dataset"),
               lastName = record.get("last_name"),
               firstNameMi = record.get("first_name_mi").trimToNull(),
               passCode = record.get("pass_code"),
               store = store,
               active = record.get("active")?.toBoolean() ?: true,
               allowAutoStoreAssign = record.get("allow_auto_store_assign")?.toBoolean() ?: false
            )
         )
      } else {
         logger.error("Skipping {}", record) // TODO determine if throwing an exception is better here, which would cause the program to exit
      }
   }
}
