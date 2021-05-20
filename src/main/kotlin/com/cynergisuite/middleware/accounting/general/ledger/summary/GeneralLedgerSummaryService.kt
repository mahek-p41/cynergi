package com.cynergisuite.middleware.accounting.general.ledger.summary

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.company.Company
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerSummaryService @Inject constructor(
   private val generalLedgerSummaryRepository: GeneralLedgerSummaryRepository,
   private val generalLedgerSummaryValidator: GeneralLedgerSummaryValidator
) {
   fun fetchOne(id: UUID, company: Company): GeneralLedgerSummaryDTO? {
      return generalLedgerSummaryRepository.findOne(id, company)?.let { transformEntity(it) }
   }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<GeneralLedgerSummaryDTO> {
      val found = generalLedgerSummaryRepository.findAll(company, pageRequest)

      return found.toPage { generalLedgerSummary: GeneralLedgerSummaryEntity ->
         GeneralLedgerSummaryDTO(generalLedgerSummary)
      }
   }

   fun create(dto: GeneralLedgerSummaryDTO, company: Company): GeneralLedgerSummaryDTO {
      val toCreate = generalLedgerSummaryValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerSummaryRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: GeneralLedgerSummaryDTO, company: Company): GeneralLedgerSummaryDTO {
      val toUpdate = generalLedgerSummaryValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerSummaryRepository.update(toUpdate, company))
   }

   private fun transformEntity(generalLedgerSummary: GeneralLedgerSummaryEntity): GeneralLedgerSummaryDTO {
      return GeneralLedgerSummaryDTO(entity = generalLedgerSummary)
   }
}
