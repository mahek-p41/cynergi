package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.permission.infrastructure.AuditPermissionTypeRepository
import com.cynergisuite.middleware.authentication.User
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import javax.inject.Singleton

@Singleton
class AuditPermissionValidator(
   private val companyRepository: CompanyRepository,
   private val departmentRepository: DepartmentRepository,
   private val permissionTypeRepository: AuditPermissionTypeRepository
) : ValidatorBase() {

   @Throws(ValidationException::class)
   fun validateCreate(permission: AuditPermissionCreateUpdateDataTransferObject, user: User): AuditPermissionEntity {
      doValidation { errors ->
         val permissionTypeId = permission.permissionType!!.myId()!!
         val departmentId = permission.department!!.myId()!!

         if (permissionTypeRepository.doesNotExist(permissionTypeId)) {
            errors.add(ValidationError("permission.id", NotFound(permissionTypeId)))
         }

         if (departmentRepository.doesNotExist(departmentId)) {
            errors.add(ValidationError("department.id", NotFound(departmentId)))
         }
      }

      val permissionType = permissionTypeRepository.findOne(permission.permissionType!!.myId()!!)!!
      val company = companyRepository.findByDataset(user.myDataset())!!
      val department = departmentRepository.findOne(permission.department!!.myId()!!)!!

      return AuditPermissionEntity(
         type = permissionType,
         company = company,
         department = department
      )
   }
}
