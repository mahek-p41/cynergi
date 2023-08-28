package com.cynergisuite.middleware.security

import com.cynergisuite.middleware.authentication.user.SecurityGroup
import com.cynergisuite.middleware.authentication.user.SecurityGroupDTO
import com.cynergisuite.middleware.authentication.user.infrastructure.SecurityGroupRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class SecurityService @Inject constructor(
   private val securityGroupRepository: SecurityGroupRepository
) {

   fun findByEmployee(id: Long): List<SecurityGroupDTO> {
      val securityGroupList = securityGroupRepository.findAllByEmployee(id)

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

   fun create(dto: SecurityGroupDTO, company: CompanyEntity): SecurityGroupDTO {

     return SecurityGroupDTO(securityGroupRepository.insert(SecurityGroup(dto)))
   }

   fun update(id: UUID, dto: SecurityGroupDTO, company: CompanyEntity): SecurityGroupDTO {

      return SecurityGroupDTO(securityGroupRepository.update(SecurityGroup(dto)))
   }

   fun addEmployeeToSecurityGroup(employeeId: Long, securityGroupId: UUID): List<SecurityGroupDTO> {
      securityGroupRepository.assignEmployeeToSecurityGroup(employeeId, securityGroupId)
      return findByEmployee(employeeId)
   }

   fun assignAccessPointsToSecurityGroups(securityGroupId: UUID, accessPointList: List<Int>) {
      securityGroupRepository.assignAccessPointsToSecurityGroups(securityGroupId, accessPointList)
   }


}
