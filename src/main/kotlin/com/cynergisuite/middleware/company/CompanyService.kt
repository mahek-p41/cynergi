package com.cynergisuite.middleware.company

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.ValidatorBase.Companion.logger
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class CompanyService @Inject constructor(
   private val companyRepository: CompanyRepository,
) {

   fun fetchOne(id: UUID): CompanyEntity? {
      return companyRepository.findOne(id)
   }

   fun fetchByDatasetCode(datasetCode: String): CompanyDTO? =
      companyRepository.findByDataset(datasetCode)?.let { localizeAndTransformEntityToDto(it) }

   fun fetchByDatasetCodeForEntity(datasetCode: String): CompanyEntity? =
      companyRepository.findByDataset(datasetCode)

   private fun localizeAndTransformEntityToDto(entity: CompanyEntity): CompanyDTO {
      logger.trace("Searching for Company by dataset resulted in: {}", entity)
      return CompanyDTO(
         entity
      )
   }

   fun fetchAll(pageRequest: PageRequest): Page<CompanyDTO> {
      val companies = companyRepository.findAll(pageRequest)

      return companies.toPage { CompanyDTO(it) }
   }
}
