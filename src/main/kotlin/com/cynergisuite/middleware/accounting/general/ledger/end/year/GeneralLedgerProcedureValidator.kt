package com.cynergisuite.middleware.accounting.general.ledger.end.year

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerJournalRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure.GeneralLedgerReversalRepository
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.GLNotInBalance
import com.cynergisuite.middleware.localization.MustBe
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.PendingJEsFoundForCurrentFiscalYear
import com.cynergisuite.middleware.localization.PendingReversalsFoundForCurrentFiscalYear
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class GeneralLedgerProcedureValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val generalLedgerJournalRepository: GeneralLedgerJournalRepository,
   private val generalLedgerReversalRepository: GeneralLedgerReversalRepository,
   private val financialCalendarRepository: FinancialCalendarRepository,
   private val generalLedgerSummaryRepository: GeneralLedgerSummaryRepository,
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerProcedureValidator::class.java)

   fun validateEndCurrentYear(dto: EndYearProceduresDTO, company: CompanyEntity): EndYearProceduresDTO {
      logger.debug("Validating end year procedures {}", dto)

      doValidation { errors ->
         val currentYear = financialCalendarRepository.findFiscalYears(company).first { it.overallPeriod!!.value == "C" }
         val pendingJEs = generalLedgerJournalRepository.findPendingJournalEntriesForCurrentFiscalYear(company,
            currentYear.begin!!, currentYear.end!!)
         if (pendingJEs > 0) errors.add(ValidationError(null, PendingJEsFoundForCurrentFiscalYear(currentYear.begin!!, currentYear.end!!)))

         val pendingReversals = generalLedgerReversalRepository.findPendingJournalReversalEntriesForCurrentFiscalYear(company,
            currentYear.begin!!, currentYear.end!!)
         if (pendingReversals > 0) errors.add(ValidationError(null, PendingReversalsFoundForCurrentFiscalYear(currentYear.begin!!, currentYear.end!!)))

         val account = accountRepository.findOne(dto.account.id!!, company)
         if (account == null) {
            errors.add(ValidationError("account.id", NotFound(dto.account.id!!)))
         } else {
            if (account.type.value != "C") errors.add(ValidationError("account", MustBe("capital account")))
         }

         if (!generalLedgerSummaryRepository.isGLBalanceForCurrentYear(companyId = company.id!!)) errors.add(
            ValidationError(null, GLNotInBalance())
         )
      }

      return dto
   }
}
