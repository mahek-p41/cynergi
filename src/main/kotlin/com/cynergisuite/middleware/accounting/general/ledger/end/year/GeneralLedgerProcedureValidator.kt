package com.cynergisuite.middleware.accounting.general.ledger.end.year

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerJournalRepository
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.MustBe
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.PendingJEsFoundForCurrentFiscalYear
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class GeneralLedgerProcedureValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val generalLedgerJournalRepository: GeneralLedgerJournalRepository,
   private val financialCalendarRepository: FinancialCalendarRepository,
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerProcedureValidator::class.java)

   fun validateEndCurrentYear(dto: EndYearProceduresDTO, company: CompanyEntity): EndYearProceduresDTO {
      logger.debug("Validating end year procedures {}", dto)

      doValidation { errors ->
         val currentYear = financialCalendarRepository.findFiscalYears(company).first { it.overallPeriod!!.value == "C" }
         val pendingJEs = generalLedgerJournalRepository.findPendingJournalEntriesForCurrentFiscalYear(company,
            currentYear.begin!!, currentYear.end!!)
         if (pendingJEs > 0) errors.add(ValidationError(null, PendingJEsFoundForCurrentFiscalYear(currentYear.begin!!, currentYear.end!!)))

         val account = accountRepository.findOne(dto.account.id!!, company)
         account ?: errors.add(ValidationError("account.id", NotFound(dto.account.id!!)))
         if (account!!.type.value != "C") errors.add(ValidationError("account", MustBe("capital account")))
      }

      return dto
   }
}
