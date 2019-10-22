package com.cynergisuite.middleware.department

import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object DepartmentFactory {

   @JvmStatic
   private val departments: List<DepartmentEntity> = listOf(
      DepartmentEntity(id = 1, code = "RM", description = "REGIONAL MGR", securityProfile = 90001, defaultMenu = "HOMEHT"),
      DepartmentEntity(id = 2, code = "MM", description = "MARKET MGR", securityProfile = 90002, defaultMenu = "HOMEHT"),
      DepartmentEntity(id = 3, code = "SM", description = "STORE MGR", securityProfile = 90003, defaultMenu = "HOMEHT"),
      DepartmentEntity(id = 4, code = "AM", description = "ASST MGR", securityProfile = 90004, defaultMenu = "HOMEHT"),
      DepartmentEntity(id = 5, code = "AR", description = "ACCOUNT REP", securityProfile = 90005, defaultMenu = "HOMEHT"),
      DepartmentEntity(id = 6, code = "SA", description = "SALES ASSOC", securityProfile = 90006, defaultMenu = "HOMEHT"),
      DepartmentEntity(id = 7, code = "DE", description = "DELIVERY DVR", securityProfile = 90007, defaultMenu = "HOMEHT"),
      DepartmentEntity(id = 8, code = "EX", description = "EXECUTIVE", securityProfile = 90000, defaultMenu = "HOMEHT"),
      DepartmentEntity(id = 9, code = "TE", description = "TERMINATED E", securityProfile = 90008, defaultMenu = "HOMEHT"),
      DepartmentEntity(id = 10, code = "CY", description = "CYNERGI EMP", securityProfile = 0, defaultMenu = "HOMEHT")
   )

   @JvmStatic
   fun findByCode(code: String) = departments.first { it.code == code }

   @JvmStatic
   fun random() = departments.random()
}

@Singleton
@Requires(env = ["develop", "test"])
class DepartmentFactoryService(
   private val departmentRepository: DepartmentRepository
) {

   fun department(code: String) : DepartmentEntity =
      departmentRepository.findOneByCode(code) ?: throw Exception("Unable to find department $code")

   fun random(): DepartmentEntity =
      departmentRepository.findOneByCode(DepartmentFactory.random().code) ?: throw Exception("Unable to find random department")
}
