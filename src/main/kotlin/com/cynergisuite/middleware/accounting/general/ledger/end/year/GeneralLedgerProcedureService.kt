package com.cynergisuite.middleware.accounting.general.ledger.end.year

import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.StoreDTO
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class GeneralLedgerProcedureService @Inject constructor(
   private val generalLedgerProcedureValidator: GeneralLedgerProcedureValidator,
   private val generalLedgerSummaryRepository: GeneralLedgerSummaryRepository
) {

   fun endCurrentYear(dto: EndYearProceduresDTO, company: CompanyEntity): EndYearProceduresDTO {
      val toCreate = generalLedgerProcedureValidator.validateEndCurrentYear(dto, company)
      generalLedgerSummaryRepository.updateClosingBalanceForCurrentFiscalYear(company)
      if (dto.profitCenter != null) {
         val corporateNetIncome = generalLedgerSummaryRepository.calculateNetIncomeForCurrentFiscalYear(company, dto.profitCenter)
         createGLDetailForInputProfitCenter(dto.profitCenter!!)
         createGLDetailsForOtherProfitCenters(rule2)
      } else {
         createGLDetailsForEachProfitCenter(rule3)
      }

   }

   private fun createGLDetailForInputProfitCenter(profitCenter: SimpleLegacyIdentifiableDTO) {
      // create GL detail for each distribution
      it.generalLedgerRecurringDistributions.forEach { distribution ->
         glDetailDTO = GeneralLedgerDetailDTO(
            null,
            AccountDTO(distribution.generalLedgerDistributionAccount),
            filterRequest.entryDate,
            StoreDTO(distribution.generalLedgerDistributionProfitCenter),
            GeneralLedgerSourceCodeDTO(it.generalLedgerRecurring.source),
            distribution.generalLedgerDistributionAmount,
            it.generalLedgerRecurring.message,
            filterRequest.employeeNumber,
            journalEntryNumber
         )

         create(glDetailDTO, company)
         glDetailList.add(glDetailDTO)
      }
   }

}
