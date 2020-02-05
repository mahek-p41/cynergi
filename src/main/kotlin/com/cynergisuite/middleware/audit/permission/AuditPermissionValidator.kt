package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.permission.infrastructure.AuditPermissionRepository
import com.cynergisuite.middleware.authentication.User
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import javax.inject.Singleton

@Singleton
class AuditPermissionValidator(
   private val companyRepository: CompanyRepository,
   private val departmentRepository: DepartmentRepository
) : ValidatorBase() {
   fun validateCreate(permission: AuditPermissionCreateUpdateDataTransferObject, user: User): AuditPermissionEntity {
      doValidation { errors ->

      }

      return AuditPermissionEntity(permission)
   }
}
