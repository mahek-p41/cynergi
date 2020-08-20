package com.cynergisuite.middleware.company

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class CompanyService @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val companyValidator: CompanyValidator
) {

   fun fetchById(id: Long): CompanyDTO? =
      companyRepository.findOne(id)?.let { CompanyDTO(it) }

   fun fetchAll(pageRequest: PageRequest): Page<CompanyDTO> {
      val companies = companyRepository.findAll(pageRequest)

      return companies.toPage { CompanyDTO(it) }
   }

   @Validated
   fun create(@Valid companyDTO: CompanyDTO): CompanyDTO {
      val toCreate = companyValidator.validateCreate(companyDTO)

      return CompanyDTO(companyRepository.insert(toCreate))
   }

   @Validated
   fun update(id: Long, @Valid companyDTO: CompanyDTO): CompanyDTO {
      val (existing, toUpdate) = companyValidator.validateUpdate(id, companyDTO)

      return CompanyDTO(companyRepository.update(existing, toUpdate))
   }
}
