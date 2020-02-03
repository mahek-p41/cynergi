package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.middleware.audit.permission.infrastructure.AuditPermissionRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.company.CompanyFactoryService
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.department.DepartmentFactory
import com.cynergisuite.middleware.department.DepartmentFactoryService
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditPermissionFactory {

   @JvmStatic
   fun single(departmentIn: DepartmentEntity? = null, permissionTypeIn: AuditPermissionType? = null, companyIn: CompanyEntity? = null): AuditPermissionEntity {
      val company = companyIn ?: CompanyFactory.random()
      val department = departmentIn ?: DepartmentFactory.random(company.datasetCode)
      val permissionType = permissionTypeIn ?: AuditPermissionTypeFactory.random()

      return AuditPermissionEntity(
         department = department,
         type = permissionType,
         company = company
      )
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditPermissionFactoryService @Inject constructor(
   private val auditPermissionRepository: AuditPermissionRepository,
   private val companyFactoryService: CompanyFactoryService,
   private val departmentFactoryService: DepartmentFactoryService
) {
   fun single(): AuditPermissionEntity {
      return single(null, null, null).let { auditPermissionRepository.insert(it) }
   }

   fun single(departmentIn: DepartmentEntity? = null, permissionTypeIn: AuditPermissionType? = null, companyIn: CompanyEntity? = null): AuditPermissionEntity {
      val company = companyIn ?: companyFactoryService.random()
      val department = departmentIn ?: departmentFactoryService.random(company.datasetCode)

      return AuditPermissionFactory.single(department, permissionTypeIn, company)
   }
}
