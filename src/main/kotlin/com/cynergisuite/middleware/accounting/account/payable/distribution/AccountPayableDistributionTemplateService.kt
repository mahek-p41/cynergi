package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionTemplateRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class AccountPayableDistributionTemplateService @Inject constructor(
   private val accountPayableDistributionTemplateRepository: AccountPayableDistributionTemplateRepository,
   private val accountPayableDistributionDetailService: AccountPayableDistributionDetailService
) {
   fun fetchOne(id: UUID, company: CompanyEntity): AccountPayableDistributionTemplateDTO? =
      accountPayableDistributionTemplateRepository.findOne(id, company)?.let { transformEntity(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<AccountPayableDistributionTemplateDTO> {
      val found = accountPayableDistributionTemplateRepository.findAll(company, pageRequest)

      return found.toPage { entity: AccountPayableDistributionTemplateEntity ->
         transformEntity(entity)
      }
   }

   fun create(dto: AccountPayableDistributionTemplateDTO, company: CompanyEntity): AccountPayableDistributionTemplateDTO {
      val toCreate = AccountPayableDistributionTemplateEntity(dto)
      return transformEntity(accountPayableDistributionTemplateRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: AccountPayableDistributionTemplateDTO, company: CompanyEntity): AccountPayableDistributionTemplateDTO {
      val toUpdate = AccountPayableDistributionTemplateEntity(dto)

      return transformEntity(accountPayableDistributionTemplateRepository.update(toUpdate, company))
   }

   fun delete(id: UUID, company: CompanyEntity) {
      accountPayableDistributionDetailService.deleteByTemplateId(id, company)
      accountPayableDistributionTemplateRepository.delete(id, company)
   }

   private fun transformEntity(accountPayableDistributionTemplate: AccountPayableDistributionTemplateEntity): AccountPayableDistributionTemplateDTO {
      return AccountPayableDistributionTemplateDTO(entity = accountPayableDistributionTemplate)
   }
}
