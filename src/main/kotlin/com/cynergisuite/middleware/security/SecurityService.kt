package com.cynergisuite.middleware.security

import com.cynergisuite.middleware.authentication.user.SecurityEmployeeDTO
import com.cynergisuite.middleware.authentication.user.SecurityGroup
import com.cynergisuite.middleware.authentication.user.SecurityGroupDTO
import com.cynergisuite.middleware.authentication.user.SecurityType
import com.cynergisuite.middleware.authentication.user.infrastructure.SecurityGroupRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class SecurityService @Inject constructor(
   private val securityGroupRepository: SecurityGroupRepository
) {

   fun findByEmployee(id: Long, company: CompanyEntity): List<SecurityGroupDTO> {
      val securityGroupList = securityGroupRepository.findByEmployee(id, company.id!!)

      return securityGroupList.map {
         SecurityGroupDTO(it)
      }
   }
   fun findByCompany(company: CompanyEntity): List<SecurityGroupDTO> {

      val securityGroupList = securityGroupRepository.findByCompany(company.id!!)

      return securityGroupList.map {
         SecurityGroupDTO(it)
      }
   }

   fun findOne(securityGroupId: UUID) : SecurityGroup {
      return securityGroupRepository.findOne(securityGroupId)!!
   }

   fun findAllSecurityTypes(company: CompanyEntity): List<SecurityType> {
      return securityGroupRepository.findAllSecurityAccessPointTypes(company)
   }

   @Transactional
   fun create(dto: SecurityEmployeeDTO, company: CompanyEntity): SecurityGroupDTO {


      return SecurityGroupDTO( securityGroupRepository.createNewGroupAndTypes(dto))
   }

   fun update(id: UUID, dto: SecurityEmployeeDTO, company: CompanyEntity): SecurityGroupDTO {

      return SecurityGroupDTO(securityGroupRepository.update(dto))
   }

   fun addEmployeeToSecurityGroup(employee: EmployeeEntity, securityGroupId: List<UUID>, company: CompanyEntity): List<SecurityGroupDTO> {

      securityGroupRepository.assignSingleEmployeeToMultipleSecurityGroups(employee, securityGroupId)
      return findByEmployee(employee.id!!, company)
   }

   fun assignAccessPointsToSecurityGroups(securityGroupId: UUID, accessPointList: List<Int>) {
      securityGroupRepository.assignAccessPointsToSecurityGroups(securityGroupId, accessPointList)
   }


}
