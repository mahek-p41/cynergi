package com.cynergisuite.middleware.accounting.general.ledger.reversal.entry

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.infrastructure.GeneralLedgerReversalEntryRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class GeneralLedgerReversalEntryService @Inject constructor(
   private val generalLedgerReversalEntryRepository: GeneralLedgerReversalEntryRepository,
   private val generalLedgerReversalEntryValidator: GeneralLedgerReversalEntryValidator,
   private val financialCalendarRepository: FinancialCalendarRepository
) {

   fun fetchById(id: UUID, company: CompanyEntity): GeneralLedgerReversalEntryDTO? =
      generalLedgerReversalEntryRepository.findOne(id, company)?.let { GeneralLedgerReversalEntryDTO(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<GeneralLedgerReversalEntryDTO> {
      val found = generalLedgerReversalEntryRepository.findAll(company, pageRequest)

      return found.toPage { entity: GeneralLedgerReversalEntryEntity ->
         GeneralLedgerReversalEntryDTO(entity)
      }
   }

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

   fun checkReversalDate(dto: GeneralLedgerReversalEntryDTO, company: CompanyEntity): Boolean {
      val glOpenDateRange = financialCalendarRepository.findDateRangeWhenGLIsOpen(company)
      val reversalDate = dto.generalLedgerReversal!!.reversalDate

      return if (glOpenDateRange != null) {
         val glOpenBegin = glOpenDateRange.first
         val glOpenEnd = glOpenDateRange.second
         !(reversalDate!!.isBefore(glOpenDateRange.first) || reversalDate.isAfter(glOpenDateRange.second))
      } else {
         false
      }
   }

   private fun transformEntity(entity: GeneralLedgerReversalEntryEntity): GeneralLedgerReversalEntryDTO {
      return GeneralLedgerReversalEntryDTO(entity)
   }
}
