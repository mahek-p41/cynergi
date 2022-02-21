package com.cynergisuite.middleware.accounting.general.ledger.reversal.entry

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.infrastructure.GeneralLedgerReversalEntryRepository
import com.cynergisuite.middleware.company.CompanyEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerReversalEntryService @Inject constructor(
   private val generalLedgerReversalEntryRepository: GeneralLedgerReversalEntryRepository,
   private val generalLedgerReversalEntryValidator: GeneralLedgerReversalEntryValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): GeneralLedgerReversalEntryDTO? =
      generalLedgerReversalEntryRepository.findOne(id, company)?.let { GeneralLedgerReversalEntryDTO(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<GeneralLedgerReversalEntryDTO> {
      val found = generalLedgerReversalEntryRepository.findAll(company, pageRequest)

      return found.toPage { entity: GeneralLedgerReversalEntryEntity ->
         GeneralLedgerReversalEntryDTO(entity)
      }
   }

   // will be used by Journal Entry
   fun create(dto: GeneralLedgerReversalEntryDTO, company: CompanyEntity): GeneralLedgerReversalEntryDTO {
      val toCreate = generalLedgerReversalEntryValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerReversalEntryRepository.insert(company, toCreate))
   }

   fun update(id: UUID, dto: GeneralLedgerReversalEntryDTO, company: CompanyEntity): GeneralLedgerReversalEntryDTO {
      val toUpdate = generalLedgerReversalEntryValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerReversalEntryRepository.update(company, toUpdate))
   }

   fun delete(id: UUID, company: CompanyEntity) {
      generalLedgerReversalEntryRepository.delete(id, company)
   }

   private fun transformEntity(entity: GeneralLedgerReversalEntryEntity): GeneralLedgerReversalEntryDTO {
      return GeneralLedgerReversalEntryDTO(entity)
   }
}
