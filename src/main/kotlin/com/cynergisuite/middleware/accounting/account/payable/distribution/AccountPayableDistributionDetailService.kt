package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionDetailRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class AccountPayableDistributionDetailService @Inject constructor(
   private val accountPayableDistributionDetailRepository: AccountPayableDistributionDetailRepository,
   private val accountPayableDistributionDetailValidator: AccountPayableDistributionDetailValidator
) {
   fun fetchOne(id: UUID, company: CompanyEntity): AccountPayableDistributionDetailDTO? =
      accountPayableDistributionDetailRepository.findOne(id, company)?.let { transformEntity(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<AccountPayableDistributionDetailDTO> {
      val found = accountPayableDistributionDetailRepository.findAll(company, pageRequest)

      return found.toPage { entity: AccountPayableDistributionDetailEntity ->
         transformEntity(entity)
      }
   }

   fun fetchAllRecordsByTemplateId(company: CompanyEntity, id: UUID, pageRequest: PageRequest): Page<AccountPayableDistributionDetailDTO> {
      val found = accountPayableDistributionDetailRepository.findAllRecordsByGroup(company, id, pageRequest)

      return found.toPage { entity: AccountPayableDistributionDetailEntity ->
         transformEntity(entity)
      }
   }

   fun create(dto: AccountPayableDistributionDetailDTO, company: CompanyEntity): AccountPayableDistributionDetailDTO {
      val toCreate = accountPayableDistributionDetailValidator.validateCreate(dto, company)

      return transformEntity(accountPayableDistributionDetailRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: AccountPayableDistributionDetailDTO, company: CompanyEntity): AccountPayableDistributionDetailDTO {
      val toUpdate = accountPayableDistributionDetailValidator.validateUpdate(id, dto, company)

      return transformEntity(accountPayableDistributionDetailRepository.update(toUpdate, company))
   }

   fun update(dto: List<AccountPayableDistributionDetailDTO>, company: CompanyEntity): List<AccountPayableDistributionDetailDTO> {

      val toUpdate = accountPayableDistributionDetailValidator.validateBulkUpdate(dto, company)
      val updated = accountPayableDistributionDetailRepository.bulkUpdate(toUpdate, company)
      return updated.map { transformEntity(it) }.toList()
   }
   fun delete(id: UUID, company: CompanyEntity) {
      accountPayableDistributionDetailRepository.delete(id, company)
   }

   fun deleteByTemplateId(id: UUID, company: CompanyEntity) {
      accountPayableDistributionDetailRepository.deleteByTemplateId(id, company)
   }
   private fun transformEntity(accountPayableDistribution: AccountPayableDistributionDetailEntity): AccountPayableDistributionDetailDTO {
      return AccountPayableDistributionDetailDTO(entity = accountPayableDistribution)
   }
}
