package com.cynergisuite.middleware.department

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartmentService @Inject constructor(
   private val departmentRepository: DepartmentRepository
) {

   fun fetchOne(id: Long): DepartmentValueObject? =
      departmentRepository.findOne(id)?.let { DepartmentValueObject(it) }

   fun fetchOneByCode(code: String): DepartmentValueObject? =
      departmentRepository.findOneByCode(code)?.let { DepartmentValueObject(it) }

   fun fetchAll(pageRequest: PageRequest): Page<DepartmentValueObject> {
      val departments = departmentRepository.findAll(pageRequest)

      return departments.toPage(pageRequest) { DepartmentValueObject(it) }
   }
}
