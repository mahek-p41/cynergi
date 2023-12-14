package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class EmployeeService @Inject constructor(
   private val employeeRepository: EmployeeRepository
) {

   fun fetchOne(id: Long, company: CompanyEntity): EmployeeEntity {
      return employeeRepository.findOne(id, company)!!

   }
   fun fetchAll(pageRequest: EmployeePageRequest, company: CompanyEntity): Page<EmployeeValueObject> {
      val employees = employeeRepository.findAll(pageRequest, company)

      return employees.toPage { employee ->
         EmployeeValueObject(employee)
      }
   }

   fun fetchAllBySecurityGroup(securityGroupId: UUID, company: CompanyEntity): List<EmployeeValueObject> {
      val employees = employeeRepository.findEmployeesBySecurityGroup(securityGroupId, company)
      return employees.map {
         EmployeeValueObject(it)
      }
   }

   fun fetchAllByAccessPoint(accessPointId: Int, company: CompanyEntity): List<EmployeeValueObject> {
      val employees = employeeRepository.findEmployeesByAccessPoint(accessPointId, company)
      return employees.map {
         EmployeeValueObject(it)
      }
   }
   fun fetchPurchaseOrderApprovers(user: User): List<EmployeeValueObject> {
      return employeeRepository.findPurchaseOrderApprovers(user).map { EmployeeValueObject(it) }
   }
}
