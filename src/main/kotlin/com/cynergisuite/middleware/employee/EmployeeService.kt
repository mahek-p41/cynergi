package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeService @Inject constructor(
   private val employeeRepository: EmployeeRepository
) {

   fun fetchAll(pageRequest: EmployeePageRequest, company: Company): Page<EmployeeValueObject> {
      val employees = employeeRepository.findAll(pageRequest, company)

      return employees.toPage { employee ->
         EmployeeValueObject(employee)
      }
   }
}
