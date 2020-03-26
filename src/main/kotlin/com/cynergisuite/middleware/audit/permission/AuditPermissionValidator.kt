package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.permission.infrastructure.AuditPermissionRepository
import com.cynergisuite.middleware.audit.permission.infrastructure.AuditPermissionTypeRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import javax.inject.Singleton

@Singleton
class AuditPermissionValidator(
   private val auditPermissionRepository: AuditPermissionRepository,
   private val auditPermissionTypeRepository: AuditPermissionTypeRepository,
   private val companyRepository: CompanyRepository,
   private val departmentRepository: DepartmentRepository
) : ValidatorBase() {

   @Throws(ValidationException::class)
   fun validateCreate(permission: AuditPermissionCreateDataTransferObject, user: User): AuditPermissionEntity {
      doValidation { errors ->
         val permissionTypeId = permission.permissionType!!.myId()!!
         val departmentId = permission.department!!.myId()!!

         if (auditPermissionTypeRepository.doesNotExist(permissionTypeId)) {
            errors.add(ValidationError("permission.id", NotFound(permissionTypeId)))
         }

         if (departmentRepository.doesNotExist(departmentId, user.myCompany())) {
            errors.add(ValidationError("department.id", NotFound(departmentId)))
         }
      }

      val company = user.myCompany()
      val permissionType = auditPermissionTypeRepository.findOne(permission.permissionType!!.myId()!!)!!
      val department = departmentRepository.findOne(permission.department!!.myId()!!, company)!!

      return AuditPermissionEntity(
         type = permissionType,
         department = department
      )
   }
}
