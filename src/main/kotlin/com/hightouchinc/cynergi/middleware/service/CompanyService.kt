package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.entity.CompanyDto
import com.hightouchinc.cynergi.middleware.repository.CompanyRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyService @Inject constructor(
   private val companyRepository: CompanyRepository
): IdentifiableService<CompanyDto> {

   override fun fetchById(id: Long): CompanyDto? =
      companyRepository.findOne(id = id)?.let { CompanyDto(it) }

   override fun exists(id: Long): Boolean =
      companyRepository.exists(id = id)
}
