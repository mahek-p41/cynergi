package com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.infrastructure.GeneralLedgerReversalDistributionRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class GeneralLedgerReversalDistributionService @Inject constructor(
   private val generalLedgerReversalDistributionRepository: GeneralLedgerReversalDistributionRepository,
   private val generalLedgerReversalDistributionValidator: GeneralLedgerReversalDistributionValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): GeneralLedgerReversalDistributionDTO? =
      generalLedgerReversalDistributionRepository.findOne(id, company)?.let { GeneralLedgerReversalDistributionDTO(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<GeneralLedgerReversalDistributionDTO> {
      val found = generalLedgerReversalDistributionRepository.findAll(company, pageRequest)

      return found.toPage { entity: GeneralLedgerReversalDistributionEntity ->
         GeneralLedgerReversalDistributionDTO(entity)
      }
   }

   fun fetchAllByReversalId(glReversalId: UUID, company: CompanyEntity, pageRequest: PageRequest): Page<GeneralLedgerReversalDistributionDTO> {
      val found = generalLedgerReversalDistributionRepository.findAllByReversalId(glReversalId, company, pageRequest)

      return found.toPage { entity: GeneralLedgerReversalDistributionEntity ->
         GeneralLedgerReversalDistributionDTO(entity)
      }
   }

   fun create(dto: GeneralLedgerReversalDistributionDTO, company: CompanyEntity): GeneralLedgerReversalDistributionDTO {
      val toCreate = generalLedgerReversalDistributionValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerReversalDistributionRepository.insert(toCreate))
   }

   fun update(id: UUID, dto: GeneralLedgerReversalDistributionDTO, company: CompanyEntity): GeneralLedgerReversalDistributionDTO {
      val toUpdate = generalLedgerReversalDistributionValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerReversalDistributionRepository.update(toUpdate))
   }

   fun delete(id: UUID, company: CompanyEntity) {
      generalLedgerReversalDistributionRepository.delete(id)
   }

   private fun transformEntity(entity: GeneralLedgerReversalDistributionEntity): GeneralLedgerReversalDistributionDTO {
      return GeneralLedgerReversalDistributionDTO(entity)
   }
}
