package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.entity.EmployeeDto
import com.hightouchinc.cynergi.middleware.repository.EmployeeRepository
import javax.inject.Singleton

@Singleton
class EmployeeService(
   private val employeeRepository: EmployeeRepository
) : IdentifiableService<EmployeeDto> {

   override fun fetchById(id: Long): EmployeeDto? =
      employeeRepository.findOne(id = id)?.let { EmployeeDto(it) }

   override fun exists(id: Long): Boolean =
      employeeRepository.exists(id = id)
}
