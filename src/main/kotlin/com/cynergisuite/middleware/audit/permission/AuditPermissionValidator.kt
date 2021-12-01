package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.permission.infrastructure.AuditPermissionTypeRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import jakarta.inject.Singleton

@Singleton
class AuditPermissionValidator(
   private val auditPermissionTypeRepository: AuditPermissionTypeRepository,
   private val departmentRepository: DepartmentRepository
) : ValidatorBase() {

   @Throws(ValidationException::class)
   fun validateCreate(permission: AuditPermissionCreateDTO, user: User): AuditPermissionEntity {
      doValidation { errors ->
         val permissionTypeId = permission.permissionType!!.id!!
         val departmentId = permission.department!!.myId()!!

         if (auditPermissionTypeRepository.doesNotExist(permissionTypeId)) {
            errors.add(ValidationError("permission.id", NotFound(permissionTypeId)))
         }

         if (departmentRepository.doesNotExist(departmentId, user.myCompany())) {
            errors.add(ValidationError("department.id", NotFound(departmentId)))
         }
      }

      val company = user.myCompany()
      val permissionType = auditPermissionTypeRepository.findOne(permission.permissionType!!.id!!)!!
      val department = departmentRepository.findOne(permission.department!!.myId()!!, company)!!

      return AuditPermissionEntity(
         type = permissionType,
         department = department
      )
   }
}
