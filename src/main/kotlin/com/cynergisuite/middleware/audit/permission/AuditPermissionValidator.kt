package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.permission.infrastructure.AuditPermissionRepository
import com.cynergisuite.middleware.audit.permission.infrastructure.AuditPermissionTypeRepository
import com.cynergisuite.middleware.authentication.User
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import javax.inject.Singleton

@Singleton
class AuditPermissionValidator(
   private val auditPermissionRepository: AuditPermissionRepository,
   private val auditPermissionTypeRepository: AuditPermissionTypeRepository,
   private val companyRepository: CompanyRepository,
   private val departmentRepository: DepartmentRepository
) : ValidatorBase() {

   @Throws(ValidationException::class)
   fun validateCreate(permission: AuditPermissionCreateUpdateDataTransferObject, user: User): AuditPermissionEntity {
      doValidation { errors ->
         val permissionTypeId = permission.permissionType!!.myId()!!
         val departmentId = permission.department!!.myId()!!

         if (auditPermissionTypeRepository.doesNotExist(permissionTypeId)) {
            errors.add(ValidationError("permission.id", NotFound(permissionTypeId)))
         }

         if (departmentRepository.doesNotExist(departmentId)) {
            errors.add(ValidationError("department.id", NotFound(departmentId)))
         }
      }

      val permissionType = auditPermissionTypeRepository.findOne(permission.permissionType!!.myId()!!)!!
      val company = companyRepository.findByDataset(user.myDataset())!!
      val department = departmentRepository.findOne(permission.department!!.myId()!!)!!

      return AuditPermissionEntity(
         type = permissionType,
         company = company,
         department = department
      )
   }

   fun validateUpdate(permission: AuditPermissionCreateUpdateDataTransferObject, user: User): AuditPermissionEntity {
      doValidation { errors ->
         val id = permission.id
         val permissionTypeId = permission.permissionType!!.myId()!!
         val departmentId = permission.department!!.myId()!!

         if (id == null) {
            errors.add(ValidationError("id", NotNull("id")))
         } else if (auditPermissionRepository.doesNotExist(id)) {
            errors.add(ValidationError("id", NotFound(id)))
         }

         if (auditPermissionTypeRepository.doesNotExist(permissionTypeId)) {
            errors.add(ValidationError("permission.id", NotFound(permissionTypeId)))
         }

         if (departmentRepository.doesNotExist(departmentId)) {
            errors.add(ValidationError("department.id", NotFound(departmentId)))
         }
      }

      val existingPermission = auditPermissionRepository.findById(permission.id!!, user.myDataset())!!
      val permissionType = auditPermissionTypeRepository.findOne(permission.permissionType!!.myId()!!)!!
      val department = departmentRepository.findOne(permission.department!!.myId()!!)!!

      return existingPermission.copy(
         type = permissionType,
         department = department
      )
   }
}
