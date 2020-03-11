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
      val department = departmentIn ?: DepartmentFactory.random(company)
      val permissionType = permissionTypeIn ?: AuditPermissionTypeFactory.random()

      return AuditPermissionEntity(
         department = department,
         type = permissionType
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
      return single(null, null, null)
   }

   fun single(departmentIn: DepartmentEntity? = null, permissionTypeIn: AuditPermissionType? = null, companyIn: CompanyEntity? = null): AuditPermissionEntity {
      val company = companyIn ?: companyFactoryService.random()
      val department = departmentIn ?: departmentFactoryService.random(company)

      return AuditPermissionFactory.single(department, permissionTypeIn, company).let { auditPermissionRepository.insert(it) }
   }

   fun stream(numberIn: Int = 1, companyIn: CompanyEntity? = null, excludePermission: AuditPermissionType? = null): Stream<AuditPermissionEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val company = companyIn ?: companyFactoryService.random()
      val departmentPermissionCombos = mutableSetOf<Pair<DepartmentEntity, AuditPermissionType>>()

      return IntStream.range(0, number).mapToObj {
         val (department, type) = findDepartmentPermission(departmentPermissionCombos, company, excludePermission)

         AuditPermissionEntity(
            department = department,
            type = type
         )
      }.map { auditPermissionRepository.insert(it) }
   }

   private fun findDepartmentPermission(departmentPermissionCombos: MutableSet<Pair<DepartmentEntity, AuditPermissionType>>, company: CompanyEntity, excludePermission: AuditPermissionType?): Pair<DepartmentEntity, AuditPermissionType> {
      var department: DepartmentEntity?
      var auditPermissionType: AuditPermissionType?
      var tryCount = 0

      do {
         if (tryCount > 100) {
            throw Exception("Unable to create enough unique department and permission type combinations")
         }

         department = departmentFactoryService.random(company)
         auditPermissionType = if (excludePermission != null) AuditPermissionTypeFactory.random(excludePermission) else AuditPermissionTypeFactory.random()

         tryCount++
      } while(departmentPermissionCombos.contains(department to auditPermissionType))

      departmentPermissionCombos.add(department!! to auditPermissionType!!)

      return department to auditPermissionType
   }
}
