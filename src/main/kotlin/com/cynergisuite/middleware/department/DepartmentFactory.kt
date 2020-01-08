package com.cynergisuite.middleware.department

import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object DepartmentFactory {

   @JvmStatic
   private val departments: List<DepartmentEntity> = listOf(
      DepartmentEntity(id = 1, code = "AM", description = "ASST MGR", securityProfile = 90004, defaultMenu = "HOMEHT", dataset = "tstds1"),
      DepartmentEntity(id = 2, code = "AR", description = "ACCOUNT REP", securityProfile = 90005, defaultMenu = "HOMEHT", dataset = "tstds1"),
      DepartmentEntity(id = 3, code = "DE", description = "DELIVERY DVR", securityProfile = 90007, defaultMenu = "HOMEHT", dataset = "tstds1"),
      DepartmentEntity(id = 4, code = "EX", description = "EXECUTIVE", securityProfile = 90000, defaultMenu = "HOMEHT", dataset = "tstds1"),
      DepartmentEntity(id = 5, code = "MM", description = "MARKET MGR", securityProfile = 90002, defaultMenu = "HOMEHT", dataset = "tstds1"),
      DepartmentEntity(id = 6, code = "RM", description = "REGIONAL MGR", securityProfile = 90001, defaultMenu = "HOMEHT", dataset = "tstds1"),
      DepartmentEntity(id = 7, code = "SA", description = "SALES ASSOC", securityProfile = 90006, defaultMenu = "HOMEHT", dataset = "tstds1"),
      DepartmentEntity(id = 8, code = "SM", description = "STORE MGR", securityProfile = 90003, defaultMenu = "HOMEHT", dataset = "tstds1"),
      DepartmentEntity(id = 9, code = "TE", description = "TERMINATED", securityProfile = 90008, defaultMenu = "HOMEHT", dataset = "tstds1"),

      DepartmentEntity(id = 10, code = "AM", description = "ACCOUNT MGR", securityProfile = 0, defaultMenu = "", dataset = "tstds2"),
      DepartmentEntity(id = 11, code = "CO", description = "COLLECTIONS", securityProfile = 0, defaultMenu = "", dataset = "tstds2"),
      DepartmentEntity(id = 12, code = "DE", description = "DELIVERY", securityProfile = 0, defaultMenu = "", dataset = "tstds2"),
      DepartmentEntity(id = 13, code = "DM", description = "DISTRICT MGR", securityProfile = 0, defaultMenu = "", dataset = "tstds2"),
      DepartmentEntity(id = 14, code = "MG", description = "MANAGEMENT", securityProfile = 0, defaultMenu = "", dataset = "tstds2"),
      DepartmentEntity(id = 15, code = "OF", description = "OFFICE", securityProfile = 0, defaultMenu = "", dataset = "tstds2"),
      DepartmentEntity(id = 16, code = "SA", description = "SALES ASSOCI", securityProfile = 0, defaultMenu = "", dataset = "tstds2"),
      DepartmentEntity(id = 17, code = "SM", description = "STORE MANAGE", securityProfile = 0, defaultMenu = "", dataset = "tstds2"),
      DepartmentEntity(id = 18, code = "WH", description = "WAREHOUS", securityProfile = 0, defaultMenu = "", dataset = "tstds2")
   )

   @JvmStatic
   fun findByCode(code: String) = departments.first { it.code == code }

   @JvmStatic
   fun random() = departments.random()

   @JvmStatic
   fun all(): List<DepartmentEntity> = all("tstds1")

   @JvmStatic
   fun all(dataset:String = "tstds1"): List<DepartmentEntity> = departments.filter { it.dataset == dataset }
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
