package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.middleware.audit.permission.infrastructure.AuditPermissionRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.company.CompanyFactoryService
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.department.DepartmentFactory
import com.cynergisuite.middleware.department.DepartmentFactoryService
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import javax.inject.Inject
import javax.inject.Singleton

@CompileStatic
class AuditPermissionTestDataLoader {

   static AuditPermissionEntity single(DepartmentEntity departmentIn = null, AuditPermissionType permissionTypeIn = null, CompanyEntity companyIn = null) {
      final company = companyIn ?: CompanyFactory.random()
      final department = departmentIn ?: DepartmentFactory.random(company)
      final permissionType = permissionTypeIn ?: AuditPermissionTypeTestDataLoader.random()

      return new AuditPermissionEntity(
         null,
         permissionType,
         department,
      )
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AuditPermissionTestDataLoaderService {
   private final AuditPermissionRepository auditPermissionRepository
   private final CompanyFactoryService companyFactoryService
   private final DepartmentFactoryService departmentFactoryService

   @Inject
   AuditPermissionTestDataLoaderService(AuditPermissionRepository auditPermissionRepository, CompanyFactoryService companyFactoryService, DepartmentFactoryService departmentFactoryService) {
      this.auditPermissionRepository = auditPermissionRepository
      this.companyFactoryService = companyFactoryService
      this.departmentFactoryService = departmentFactoryService
   }

   AuditPermissionEntity single(DepartmentEntity departmentIn = null, AuditPermissionType permissionTypeIn = null, CompanyEntity companyIn = null) {
      final company = companyIn ?: companyFactoryService.random()
      final department = departmentIn ?: departmentFactoryService.random(company)

      return AuditPermissionTestDataLoader.single(department, permissionTypeIn, company).with { auditPermissionRepository.insert(it) }
   }
}
