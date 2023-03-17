package com.cynergisuite.middleware.accounting.general.ledger.end.year

import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class GeneralLedgerProcedureService @Inject constructor(
   private val generalLedgerProcedureValidator: GeneralLedgerProcedureValidator
) {

   fun endCurrentYear(dto: EndYearProceduresDTO, company: CompanyEntity): EndYearProceduresDTO {
      val toCreate = generalLedgerProcedureValidator.validateEndCurrentYear(dto, company)

   }

}
