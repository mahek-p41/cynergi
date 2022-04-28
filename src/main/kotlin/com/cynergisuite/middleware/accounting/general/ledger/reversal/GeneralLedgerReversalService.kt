package com.cynergisuite.middleware.accounting.general.ledger.reversal

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure.GeneralLedgerReversalRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class GeneralLedgerReversalService @Inject constructor(
   private val generalLedgerReversalRepository: GeneralLedgerReversalRepository,
   private val generalLedgerReversalValidator: GeneralLedgerReversalValidator
) {
   fun fetchOne(id: UUID, company: CompanyEntity): GeneralLedgerReversalDTO? {
      return generalLedgerReversalRepository.findOne(id, company)?.let { transformEntity(it) }
   }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<GeneralLedgerReversalDTO> {
      val found = generalLedgerReversalRepository.findAll(pageRequest, company)

      return found.toPage { generalLedgerReversal: GeneralLedgerReversalEntity ->
         GeneralLedgerReversalDTO(generalLedgerReversal)
      }
   }

   fun create(dto: GeneralLedgerReversalDTO, company: CompanyEntity): GeneralLedgerReversalDTO {
      val toCreate = generalLedgerReversalValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerReversalRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: GeneralLedgerReversalDTO, company: CompanyEntity): GeneralLedgerReversalDTO {
      val toUpdate = generalLedgerReversalValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerReversalRepository.update(toUpdate, company))
   }

   fun delete(id: UUID, company: CompanyEntity) {
      generalLedgerReversalRepository.delete(id, company)
   }

   private fun transformEntity(generalLedgerReversal: GeneralLedgerReversalEntity): GeneralLedgerReversalDTO {
      return GeneralLedgerReversalDTO(entity = generalLedgerReversal)
   }
}
