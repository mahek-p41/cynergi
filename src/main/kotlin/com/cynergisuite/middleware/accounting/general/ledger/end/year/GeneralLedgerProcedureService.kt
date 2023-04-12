package com.cynergisuite.middleware.accounting.general.ledger.end.year

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.end.year.infrastructure.GeneralLedgerProcedureRepository
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.authentication.user.User
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class GeneralLedgerProcedureService @Inject constructor(
   private val generalLedgerProcedureValidator: GeneralLedgerProcedureValidator,
   private val generalLedgerSummaryRepository: GeneralLedgerSummaryRepository,
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
   private val financialCalendarRepository: FinancialCalendarRepository,
   private val generalLedgerProcedureRepository: GeneralLedgerProcedureRepository,
) {

   @Transactional
   fun endCurrentYear(dto: EndYearProceduresDTO, user: User) {
      val company = user.myCompany()
      generalLedgerProcedureValidator.validateEndCurrentYear(dto, company)
      generalLedgerSummaryRepository.updateClosingBalanceForCurrentFiscalYear(company)

      createBalanceForwardGLDetailsForAssetLiabilityCapitalAccounts(user, dto.account)
      createBalanceForwardGLDetailsForRetainedEarningsAccount(user, dto)

      generalLedgerSummaryRepository.rollOneFinancialYear(company)
      financialCalendarRepository.rollOneFinancialYear(company)
   }

   private fun createBalanceForwardGLDetailsForRetainedEarningsAccount(user: User, dto: EndYearProceduresDTO) {
      val company = user.myCompany()
      val jeNumber = generalLedgerDetailRepository.findNextJENumber(company)
      val firstDateOfNextFiscalYear = financialCalendarRepository.findFirstDateOfFiscalYear(company, 4)
      if (dto.profitCenter != null) {
         val corporateNetIncome = generalLedgerSummaryRepository.calculateNetIncomeForCurrentFiscalYear(company, dto.account)
         val params = mapOf(
            "comp_id" to company.id!!,
            "emp_number" to user.myEmployeeNumber(),
            "je_number" to jeNumber,
            "gl_date" to firstDateOfNextFiscalYear,
            "retained_earnings_account" to dto.account.id!!,
            "corporate_net_income" to corporateNetIncome,
            "profit_center" to dto.profitCenter!!.id!!
         )
         generalLedgerProcedureRepository.createBalanceForwardGLDetailsForRetainedEarningsAccountForCorporateProfitCenter(params)
         generalLedgerProcedureRepository.createBalanceForwardGLDetailsForRetainedEarningsAccountForOtherProfitCenters(params)
      } else {
         val params = mapOf(
            "comp_id" to company.id!!,
            "emp_number" to user.myEmployeeNumber(),
            "gl_date" to firstDateOfNextFiscalYear,
            "retained_earnings_account" to dto.account.id!!,
         )
         generalLedgerProcedureRepository.createBalanceForwardGLDetailsForRetainedEarningsAccountForEachProfitCenter(params)
      }
   }

   private fun createBalanceForwardGLDetailsForAssetLiabilityCapitalAccounts(
      user: User,
      retainedEarningsAccount: SimpleIdentifiableDTO
   ) {
      val company = user.myCompany()
      val jeNumber = generalLedgerDetailRepository.findNextJENumber(company)
      val firstDateOfNextFiscalYear = financialCalendarRepository.findFirstDateOfFiscalYear(company, 4)
      val params = mapOf(
         "comp_id" to company.id!!,
         "emp_number" to user.myEmployeeNumber(),
         "je_number" to jeNumber,
         "gl_date" to firstDateOfNextFiscalYear,
         "retained_earnings_account" to retainedEarningsAccount.id!!,
      )
      generalLedgerProcedureRepository.createBalanceForwardGLDetailsForAssetLiabilityCapitalAccounts(params)
   }

}
