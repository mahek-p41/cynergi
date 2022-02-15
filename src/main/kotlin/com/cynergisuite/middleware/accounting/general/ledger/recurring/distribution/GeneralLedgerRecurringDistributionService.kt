package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.infrastructure.GeneralLedgerRecurringDistributionRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class GeneralLedgerRecurringDistributionService @Inject constructor(
   private val generalLedgerRecurringDistributionRepository: GeneralLedgerRecurringDistributionRepository,
   private val generalLedgerRecurringDistributionValidator: GeneralLedgerRecurringDistributionValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): GeneralLedgerRecurringDistributionDTO? =
      generalLedgerRecurringDistributionRepository.findOne(id, company)?.let { GeneralLedgerRecurringDistributionDTO(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<GeneralLedgerRecurringDistributionDTO> {
      val found = generalLedgerRecurringDistributionRepository.findAll(company, pageRequest)

      return found.toPage { entity: GeneralLedgerRecurringDistributionEntity ->
         GeneralLedgerRecurringDistributionDTO(entity)
      }
   }

   fun fetchAllByRecurringId(glRecurringId: UUID, company: CompanyEntity, pageRequest: PageRequest): Page<GeneralLedgerRecurringDistributionDTO> {
      val found = generalLedgerRecurringDistributionRepository.findAllByRecurringId(glRecurringId, company, pageRequest)

      return found.toPage { entity: GeneralLedgerRecurringDistributionEntity ->
         GeneralLedgerRecurringDistributionDTO(entity)
      }
   }

   fun create(dto: GeneralLedgerRecurringDistributionDTO, company: CompanyEntity): GeneralLedgerRecurringDistributionDTO {
      val toCreate = generalLedgerRecurringDistributionValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerRecurringDistributionRepository.insert(toCreate))
   }

   fun update(id: UUID, dto: GeneralLedgerRecurringDistributionDTO, company: CompanyEntity): GeneralLedgerRecurringDistributionDTO {
      val toUpdate = generalLedgerRecurringDistributionValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerRecurringDistributionRepository.update(toUpdate))
   }

   fun delete(id: UUID, company: CompanyEntity) {
      generalLedgerRecurringDistributionRepository.delete(id)
   }

   private fun transformEntity(entity: GeneralLedgerRecurringDistributionEntity): GeneralLedgerRecurringDistributionDTO {
      return GeneralLedgerRecurringDistributionDTO(entity)
   }
}
