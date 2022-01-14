package com.cynergisuite.middleware.company

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import java.util.UUID
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class CompanyService @Inject constructor(
   private val companyRepository: CompanyRepository,
) {

   fun fetchOne(id: UUID): CompanyEntity? {
      return companyRepository.findOne(id)
   }

   fun fetchByDatasetCode(datasetCode: String): CompanyEntity? {
      return companyRepository.findByDataset(datasetCode)
   }

   fun fetchAll(pageRequest: PageRequest): Page<CompanyDTO> {
      val companies = companyRepository.findAll(pageRequest)

      return companies.toPage { CompanyDTO(it) }
   }
}
