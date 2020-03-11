package com.cynergisuite.middleware.department

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartmentService @Inject constructor(
   private val departmentRepository: DepartmentRepository
) {

   fun fetchOne(id: Long, user: User): DepartmentValueObject? =
      departmentRepository.findOne(id, user.myCompany())?.let { DepartmentValueObject(it) }

   fun fetchAll(pageRequest: PageRequest, user: User): Page<DepartmentValueObject> {
      val departments = departmentRepository.findAll(pageRequest, user.myCompany())

      return departments.toPage { DepartmentValueObject(it) }
   }
}
