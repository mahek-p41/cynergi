package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.CSVParsingService
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.legacy.load.LegacyCsvLoadingService
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
   private val employeeValidator: EmployeeValidator
) : CSVParsingService(), LegacyCsvLoadingService {
   private val logger: Logger = LoggerFactory.getLogger(EmployeeService::class.java)
   private val employeeMatcher = FileSystems.getDefault().getPathMatcher("glob:eli-employee*csv")

   fun fetchById(id: Long, loc: String): EmployeeValueObject? =
      employeeRepository.findOne(id = id, loc = loc)?.let { EmployeeValueObject(entity = it) }

   fun exists(id: Long, loc: String): Boolean =
      employeeRepository.exists(id = id, loc = loc)

   @Validated
   fun create(@Valid vo: EmployeeValueObject): EmployeeValueObject {
      employeeValidator.validateSave(vo)

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

   fun findUserByAuthentication(number: Int, passCode: String): Maybe<Employee> =
      employeeRepository.findUserByAuthentication(number, passCode)

   override fun canProcess(path: Path): Boolean =
      employeeMatcher.matches(path.fileName)

   override fun processCsvRow(record: CSVRecord) {
      create (
         EmployeeValueObject(
            loc = "int",
            number = record.get("number").toInt(),
            passCode = record.get("pass_code"),
            active = record.get("active")?.toBoolean() ?: true
         )
      )
   }
}
