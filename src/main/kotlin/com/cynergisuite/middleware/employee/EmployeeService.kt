package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.StoreService
import io.micronaut.validation.Validated
import io.reactivex.Maybe
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class EmployeeService @Inject constructor(
   private val employeeRepository: EmployeeRepository,
   private val employeeValidator: EmployeeValidator,
   private val storeService: StoreService
) {
   private val logger: Logger = LoggerFactory.getLogger(EmployeeService::class.java)

   fun exists(id: Long, employeeType: String, company: Company): Boolean =
      employeeRepository.exists(id = id, employeeType = employeeType, company = company)

   @Validated
   fun create(@Valid vo: EmployeeValueObject, company: Company): EmployeeValueObject {
      employeeValidator.validateCreate(vo)

      return EmployeeValueObject(
         entity = employeeRepository.insert(entity = EmployeeEntity(vo = vo, company = company))
      )
   }
}
