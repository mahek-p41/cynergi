package com.cynergisuite.middleware.company

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class CompanyService @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val companyValidator: CompanyValidator
) {

   fun fetchById(id: UUID): CompanyDTO? =
      companyRepository.findOne(id)?.let { CompanyDTO(it) }

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

   fun create(companyDTO: CompanyDTO): CompanyDTO {
      val toCreate = companyValidator.validateCreate(companyDTO)

      return CompanyDTO(companyRepository.insert(toCreate))
   }

   fun update(id: UUID, companyDTO: CompanyDTO): CompanyDTO {
      val (existing, toUpdate) = companyValidator.validateUpdate(id, companyDTO)

      return CompanyDTO(companyRepository.update(existing, toUpdate))
   }

   fun delete(id: UUID ) {
      companyRepository.delete(id)
   }
}
