package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class AccountPayableDistributionService @Inject constructor(
   private val accountPayableDistributionRepository: AccountPayableDistributionRepository,
   private val accountPayableDistributionValidator: AccountPayableDistributionValidator
) {
   fun fetchOne(id: UUID, company: CompanyEntity): AccountPayableDistributionDTO? =
      accountPayableDistributionRepository.findOne(id, company)?.let { transformEntity(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<AccountPayableDistributionDTO> {
      val found = accountPayableDistributionRepository.findAll(company, pageRequest)

      return found.toPage { entity: AccountPayableDistributionEntity ->
         transformEntity(entity)
      }
   }

   fun fetchAllGroups(company: CompanyEntity, pageRequest: PageRequest): Page<String> {
      val found = accountPayableDistributionRepository.findAllGroups(company, pageRequest)

      return found.toPage { it }
   }

   fun fetchAllRecordsByGroup(company: CompanyEntity, name: String, pageRequest: PageRequest): Page<AccountPayableDistributionDTO> {
      val found = accountPayableDistributionRepository.findAllRecordsByGroup(company, name, pageRequest)

      return found.toPage { entity: AccountPayableDistributionEntity ->
         transformEntity(entity)
      }
   }

   fun create(dto: AccountPayableDistributionDTO, company: CompanyEntity): AccountPayableDistributionDTO {
      val toCreate = accountPayableDistributionValidator.validateCreate(dto, company)

      return transformEntity(accountPayableDistributionRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: AccountPayableDistributionDTO, company: CompanyEntity): AccountPayableDistributionDTO {
      val toUpdate = accountPayableDistributionValidator.validateUpdate(id, dto, company)

      return transformEntity(accountPayableDistributionRepository.update(toUpdate, company))
   }

   fun delete(id: UUID, company: CompanyEntity) {
      accountPayableDistributionRepository.delete(id, company)
   }

   private fun transformEntity(accountPayableDistribution: AccountPayableDistributionEntity): AccountPayableDistributionDTO {
      return AccountPayableDistributionDTO(entity = accountPayableDistribution)
   }
}
