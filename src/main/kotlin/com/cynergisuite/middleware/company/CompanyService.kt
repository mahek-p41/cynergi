package com.cynergisuite.middleware.company

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyService @Inject constructor(
   private val companyRepository: CompanyRepository,
) {

   fun fetchAll(pageRequest: PageRequest): Page<CompanyDTO> {
      val companies = companyRepository.findAll(pageRequest)

      return companies.toPage { CompanyDTO(it) }
   }
}
