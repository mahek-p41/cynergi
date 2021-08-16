package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerJournalRepository
import com.cynergisuite.middleware.company.CompanyEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerJournalService @Inject constructor(
   private val generalLedgerJournalRepository: GeneralLedgerJournalRepository,
   private val generalLedgerJournalValidator: GeneralLedgerJournalValidator
) {
   fun fetchOne(id: UUID, company: CompanyEntity): GeneralLedgerJournalDTO? {
      return generalLedgerJournalRepository.findOne(id, company)?.let { transformEntity(it) }
   }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<GeneralLedgerJournalDTO> {
      val found = generalLedgerJournalRepository.findAll(pageRequest, company)

      return found.toPage { generalLedgerJournal: GeneralLedgerJournalEntity ->
         GeneralLedgerJournalDTO(generalLedgerJournal)
      }
   }

   fun create(dto: GeneralLedgerJournalDTO, company: CompanyEntity): GeneralLedgerJournalDTO {
      val toCreate = generalLedgerJournalValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerJournalRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: GeneralLedgerJournalDTO, company: CompanyEntity): GeneralLedgerJournalDTO {
      val toUpdate = generalLedgerJournalValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerJournalRepository.update(toUpdate, company))
   }

   private fun transformEntity(generalLedgerJournal: GeneralLedgerJournalEntity): GeneralLedgerJournalDTO {
      return GeneralLedgerJournalDTO(entity = generalLedgerJournal)
   }
}
