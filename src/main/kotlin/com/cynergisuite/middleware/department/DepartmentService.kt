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

   fun fetchOne(id: Long, company: Company): DepartmentValueObject? =
      departmentRepository.findOne(id, dataset)?.let { DepartmentValueObject(it) }

   fun fetchAll(pageRequest: PageRequest, company: Company): Page<DepartmentValueObject> {
      val departments = departmentRepository.findAll(pageRequest, dataset)

      return departments.toPage { DepartmentValueObject(it) }
   }
}
