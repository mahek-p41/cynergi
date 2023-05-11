package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.cynergisuite.domain.GeneralLedgerTrialBalanceReportFilterRequest
import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.DatesMustBeInSameFiscalYear
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class GeneralLedgerTrialBalanceValidator @Inject constructor(
   private val financialCalendarService: FinancialCalendarService,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository,
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerTrialBalanceValidator::class.java)

   @Throws(ValidationException::class)
   fun validateReport(filterRequest: GeneralLedgerTrialBalanceReportFilterRequest, company: CompanyEntity) {
      logger.debug("Validating GL Trial Balance Report {}", filterRequest)
      val beginAccount = filterRequest.beginAccount?.let { accountRepository.findByNumber(it.toLong(), company) }
      val endAccount = filterRequest.endAccount?.let { accountRepository.findByNumber(it.toLong(), company) }
      val profitCenter = filterRequest.profitCenter?.let { storeRepository.findOne(it, company) }

      doValidation { errors ->
         if (beginAccount == null && filterRequest.beginAccount != null) errors.add(ValidationError("beginAccount", NotFound(filterRequest.beginAccount!!)))
         if (endAccount == null && filterRequest.endAccount != null) errors.add(ValidationError("endAccount", NotFound(filterRequest.endAccount!!)))

         if (profitCenter == null && filterRequest.profitCenter != null) errors.add(ValidationError("profitCenter", NotFound(filterRequest.profitCenter!!)))

         val sameFiscalYear = financialCalendarService.fetchFiscalYears(company).any { it.begin!! <= filterRequest.from && filterRequest.thru!! <= it.end!! }
         if (!sameFiscalYear) errors.add(ValidationError("from & thru", DatesMustBeInSameFiscalYear(filterRequest.from!!, filterRequest.thru!!)))
      }
   }
}
