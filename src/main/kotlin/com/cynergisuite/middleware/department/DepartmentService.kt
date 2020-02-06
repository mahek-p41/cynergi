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

   fun fetchOne(id: Long, dataset: String): DepartmentValueObject? =
      departmentRepository.findOne(id, dataset)?.let { DepartmentValueObject(it) }

   fun fetchAll(pageRequest: PageRequest, dataset: String): Page<DepartmentValueObject> {
      val departments = departmentRepository.findAll(pageRequest, dataset)

      return departments.toPage { DepartmentValueObject(it) }
   }
}
