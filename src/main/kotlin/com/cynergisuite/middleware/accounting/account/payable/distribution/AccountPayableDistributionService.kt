package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionRepository
import com.cynergisuite.middleware.company.Company
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountPayableDistributionService @Inject constructor(
   private val accountPayableDistributionRepository: AccountPayableDistributionRepository,
   private val accountPayableDistributionValidator: AccountPayableDistributionValidator
) {
   fun fetchOne(id: Long, company: Company): AccountPayableDistributionDTO? =
      accountPayableDistributionRepository.findOne(id, company)?.let { transformEntity(it) }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<AccountPayableDistributionDTO> {
      val found = accountPayableDistributionRepository.findAll(company, pageRequest)

      return found.toPage { entity: AccountPayableDistributionEntity ->
         transformEntity(entity)
      }
   }

   fun create(dto: AccountPayableDistributionDTO, company: Company): AccountPayableDistributionDTO {
      val toCreate = accountPayableDistributionValidator.validateCreate(dto, company)

      return transformEntity(accountPayableDistributionRepository.insert(toCreate, company))
   }

   fun update(id: Long, dto: AccountPayableDistributionDTO, company: Company): AccountPayableDistributionDTO {
      val toUpdate = accountPayableDistributionValidator.validateUpdate(id, dto, company)

      return transformEntity(accountPayableDistributionRepository.update(toUpdate, company))
   }

   fun delete(id: Long, company: Company) {
      accountPayableDistributionRepository.delete(id, company)
   }

   private fun transformEntity(accountPayableDistribution: AccountPayableDistributionEntity): AccountPayableDistributionDTO {
      return AccountPayableDistributionDTO(entity = accountPayableDistribution)
   }
}
