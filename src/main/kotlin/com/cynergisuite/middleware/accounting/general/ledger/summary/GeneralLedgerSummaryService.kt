package com.cynergisuite.middleware.accounting.general.ledger.summary

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class GeneralLedgerSummaryService @Inject constructor(
   private val generalLedgerSummaryRepository: GeneralLedgerSummaryRepository,
   private val generalLedgerSummaryValidator: GeneralLedgerSummaryValidator
) {
   fun fetchOne(id: UUID, company: CompanyEntity): GeneralLedgerSummaryDTO? {
      return generalLedgerSummaryRepository.findOne(id, company)?.let { transformEntity(it) }
   }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<GeneralLedgerSummaryDTO> {
      val found = generalLedgerSummaryRepository.findAll(company, pageRequest)

      return found.toPage { generalLedgerSummary: GeneralLedgerSummaryEntity ->
         GeneralLedgerSummaryDTO(generalLedgerSummary)
      }
   }

   fun create(dto: GeneralLedgerSummaryDTO, company: CompanyEntity): GeneralLedgerSummaryDTO {
      val toCreate = generalLedgerSummaryValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerSummaryRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: GeneralLedgerSummaryDTO, company: CompanyEntity): GeneralLedgerSummaryDTO {
      val toUpdate = generalLedgerSummaryValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerSummaryRepository.update(toUpdate, company))
   }

   private fun transformEntity(generalLedgerSummary: GeneralLedgerSummaryEntity): GeneralLedgerSummaryDTO {
      return GeneralLedgerSummaryDTO(entity = generalLedgerSummary)
   }
}
