package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.CSVParsingService
import com.cynergisuite.domain.infrastructure.IdentifiableService
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.legacy.load.LegacyCsvLoadingService
import io.micronaut.validation.Validated
import io.reactivex.Maybe
import org.apache.commons.csv.CSVRecord
import java.nio.file.FileSystems
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class EmployeeService @Inject constructor(
   private val employeeRepository: EmployeeRepository,
   private val employeeValidator: EmployeeValidator
) : IdentifiableService<EmployeeValueObject>, CSVParsingService(), LegacyCsvLoadingService {
   private val employeeMatcher = FileSystems.getDefault().getPathMatcher("glob:eli-employee*csv")

   override fun fetchById(id: Long): EmployeeValueObject? =
      employeeRepository.findOne(id = id)?.let { EmployeeValueObject(entity = it) }

   override fun exists(id: Long): Boolean =
      employeeRepository.exists(id = id)

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

   fun canEmployeeAccess(assert: String, employee: EmployeeValueObject): Boolean {
      return employeeRepository.canEmployeeAccess(assert, employee.id!!)
   }

   fun findUserByAuthentication(number: Int, passCode: String): Maybe<Employee> =
      employeeRepository.findUserByAuthentication(number, passCode)

   override fun canProcess(path: Path): Boolean =
      employeeMatcher.matches(path.fileName)

   override fun processCsvRow(record: CSVRecord) {
      create (
         EmployeeValueObject(
            number = record.get("number").toInt(),
            passCode = record.get("pass_code"),
            active = record.get("active")?.toBoolean() ?: true
         )
      )
   }
}
