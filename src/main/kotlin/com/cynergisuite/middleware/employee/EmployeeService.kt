package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class EmployeeService @Inject constructor(
   private val employeeRepository: EmployeeRepository
) {

   fun fetchAll(pageRequest: EmployeePageRequest, company: CompanyEntity): Page<EmployeeValueObject> {
      val employees = employeeRepository.findAll(pageRequest, company)

      return employees.toPage { employee ->
         EmployeeValueObject(employee)
      }
   }

   fun fetchPurchaseOrderApprovers(user: User): List<EmployeeValueObject> {
      return employeeRepository.findPurchaseOrderApprovers(user).map { EmployeeValueObject(it) }
   }
}
