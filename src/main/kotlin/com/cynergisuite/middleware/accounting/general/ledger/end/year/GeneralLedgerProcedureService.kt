package com.cynergisuite.middleware.accounting.general.ledger.end.year

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.end.year.infrastructure.GeneralLedgerProcedureRepository
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryDTO
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryService
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigDecimal
import javax.transaction.Transactional

@Singleton
class GeneralLedgerProcedureService @Inject constructor(
   private val generalLedgerProcedureValidator: GeneralLedgerProcedureValidator,
   private val generalLedgerSummaryRepository: GeneralLedgerSummaryRepository,
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
   private val financialCalendarRepository: FinancialCalendarRepository,
   private val generalLedgerProcedureRepository: GeneralLedgerProcedureRepository,
   private val generalLedgerSummaryService: GeneralLedgerSummaryService,
   private val storeRepository: StoreRepository,
) {

   @Transactional
   fun endCurrentYear(dto: EndYearProceduresDTO, user: User) {
      val company = user.myCompany()
      generalLedgerSummaryService.recalculateGLBalance(company)
      generalLedgerProcedureValidator.validateEndCurrentYear(dto, company)
      generalLedgerSummaryRepository.updateClosingBalanceForCurrentFiscalYear(company)

      val jeNumber = generalLedgerDetailRepository.findNextJENumber(company)
      createBalEntriesForAssetLiabilityCapitalAccounts(user, dto.account, jeNumber)
      createBalEntriesForRetainedEarningsAccount(user, dto, jeNumber)

      generalLedgerSummaryRepository.updateBeginningBalanceForNextYear(company, jeNumber)

      generalLedgerSummaryRepository.rollOneFinancialYear(company)
      financialCalendarRepository.rollOneFinancialYear(company)
   }

   private fun createBalEntriesForRetainedEarningsAccount(user: User, dto: EndYearProceduresDTO, jeNumber: Int) {
      val company = user.myCompany()
      val firstDateOfNextFiscalYear = financialCalendarRepository.findFirstDateOfFiscalYear(company, 4)
      if (dto.profitCenter != null) {
         val profitCenter = storeRepository.findOne(dto.profitCenter!!.id!!, company)!!
         val corporateNetIncome = generalLedgerSummaryRepository.calculateNetIncomeForCurrentFiscalYear(company)
         val params = mapOf(
            "comp_id" to company.id!!,
            "emp_number" to user.myEmployeeNumber(),
            "je_number" to jeNumber,
            "gl_date" to firstDateOfNextFiscalYear,
            "retained_earnings_account" to dto.account.id!!,
            "corporate_net_income" to corporateNetIncome,
            "profit_center" to profitCenter.number
         )
         if (!generalLedgerSummaryService.exists(company, dto.account.id!!, profitCenter.number, 3)) {
            val summaryDTO = GeneralLedgerSummaryDTO(
               null,
               SimpleIdentifiableDTO(dto.account.id),
               dto.profitCenter,
               OverallPeriodTypeDTO("C"),
               beginningBalance = BigDecimal.ZERO,
               closingBalance = BigDecimal.ZERO
            )
            generalLedgerSummaryService.create(summaryDTO, company)
            generalLedgerSummaryService.create(summaryDTO.copy(overallPeriod = OverallPeriodTypeDTO("N")), company)
         }
         generalLedgerProcedureRepository.createBalEntryForRetainedEarningsAccountForCorporateProfitCenter(params)
         generalLedgerProcedureRepository.createBalEntriesForRetainedEarningsAccountForOtherProfitCenters(params)
      } else {
         val params = mapOf(
            "comp_id" to company.id!!,
            "emp_number" to user.myEmployeeNumber(),
            "je_number" to jeNumber,
            "gl_date" to firstDateOfNextFiscalYear,
            "retained_earnings_account" to dto.account.id!!,
         )
         generalLedgerProcedureRepository.findProfitCentersWithNetIncome(company).forEach {
            val profitCenter = storeRepository.findOne(number = it, company)!!
            if (!generalLedgerSummaryService.exists(company, dto.account.id!!, it, 3)) {
               val summaryDTO = GeneralLedgerSummaryDTO(
                  null,
                  SimpleIdentifiableDTO(dto.account.id),
                  SimpleLegacyIdentifiableDTO(profitCenter.id),
                  OverallPeriodTypeDTO("C"),
                  beginningBalance = BigDecimal.ZERO,
                  closingBalance = BigDecimal.ZERO
               )
               generalLedgerSummaryService.create(summaryDTO, company)
               generalLedgerSummaryService.create(summaryDTO.copy(overallPeriod = OverallPeriodTypeDTO("N")), company)
            }
         }
         generalLedgerProcedureRepository.createBalEntriesForRetainedEarningsAccountForEachProfitCenter(params)
      }
   }

   private fun createBalEntriesForAssetLiabilityCapitalAccounts(
      user: User,
      retainedEarningsAccount: SimpleIdentifiableDTO,
      jeNumber: Int
   ) {
      val company = user.myCompany()

      val firstDateOfNextFiscalYear = financialCalendarRepository.findFirstDateOfFiscalYear(company, 4)
      val params = mapOf(
         "comp_id" to company.id!!,
         "emp_number" to user.myEmployeeNumber(),
         "je_number" to jeNumber,
         "gl_date" to firstDateOfNextFiscalYear,
         "retained_earnings_account" to retainedEarningsAccount.id!!,
      )
      generalLedgerProcedureRepository.createBalEntriesForAssetLiabilityCapitalAccounts(params)
   }

}
