package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.CSVParsingService
import com.cynergisuite.extensions.isDigits
import com.cynergisuite.extensions.trimToNull
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

   fun fetchById(id: Long, loc: String): EmployeeValueObject? =
      employeeRepository.findOne(id = id, loc = loc)?.let { EmployeeValueObject(entity = it) }

   fun fetchByNumberAndLoc(number: Int, loc: String): EmployeeValueObject? =
      employeeRepository.findOne(number, loc)?.let { EmployeeValueObject(entity = it) }

   fun exists(id: Long, loc: String): Boolean =
      employeeRepository.exists(id = id, loc = loc)

   @Validated
   fun create(@Valid vo: EmployeeValueObject): EmployeeValueObject {
      employeeValidator.validateCreate(vo)

      return EmployeeValueObject(
         entity = employeeRepository.insert(entity = Employee(vo = vo))
      )
   }

   @Validated
   fun update(@Valid vo: EmployeeValueObject): EmployeeValueObject {
      employeeValidator.validateUpdate(vo)

      return EmployeeValueObject(
         entity = employeeRepository.update(entity = Employee(vo = vo))
      )
   }

   fun canEmployeeAccess(asset: String, employee: EmployeeValueObject): Boolean {
      logger.debug("Checking if the user {} has access to asset {}", employee, asset)

      return employeeRepository.canEmployeeAccess(employee.loc!!, asset, employee.id!!)
   }

   fun fetchUserByAuthentication(number: Int, passCode: String, storeNumber: Int? = null): Maybe<Employee> =
      employeeRepository.findUserByAuthentication(number, passCode, storeNumber)

   override fun canProcess(path: Path): Boolean =
      employeeMatcher.matches(path.fileName)

   override fun processCsvRow(record: CSVRecord) {
      val storeNumberIn = record.get("store_number")

      if ( !storeNumberIn.isNullOrBlank() && storeNumberIn.isDigits() ) {
         val storeNumber = storeNumberIn.toInt()
         val store = storeService.fetchByNumber(storeNumber)

         create (
            EmployeeValueObject(
               loc = "int",
               number = record.get("number").toInt(),
               lastName = record.get("last_name"),
               firstNameMi = record.get("first_name_mi").trimToNull(),
               passCode = record.get("pass_code"),
               store = store,
               active = record.get("active")?.toBoolean() ?: true
            )
         )
      } else {
         logger.error("Skipping {}", record) // TODO determine if throwing an exception is better here, which would cause the program to exit
      }
   }
}
