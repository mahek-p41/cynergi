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

   fun fetchById(id: Long): CompanyValueObject? =
      companyRepository.findOne(id)?.let { CompanyValueObject(it) }

   fun fetchAll(pageRequest: PageRequest): Page<CompanyValueObject> {
      val companies = companyRepository.findAll(pageRequest)

      return companies.toPage { CompanyValueObject(it) }
   }

   @Validated
   fun create(@Valid companyVO: CompanyValueObject): CompanyValueObject {
      val toCreate = companyValidator.validateCreate(companyVO)

      return CompanyValueObject(companyRepository.insert(toCreate))
   }

   @Validated
   fun update(id: Long, @Valid companyVO: CompanyValueObject): CompanyValueObject {
      val toUpdate = companyValidator.validateUpdate(id, companyVO)

      return CompanyValueObject(companyRepository.update(toUpdate))
   }
}
