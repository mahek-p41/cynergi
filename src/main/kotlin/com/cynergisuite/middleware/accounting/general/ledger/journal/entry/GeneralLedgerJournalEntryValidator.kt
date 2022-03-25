package com.cynergisuite.middleware.accounting.general.ledger.journal.entry

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure.BankReconciliationTypeRepository
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.BalanceMustBeZero
import com.cynergisuite.middleware.localization.GLNotOpen
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal

@Singleton
class GeneralLedgerJournalEntryValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val bankReconciliationTypeRepository: BankReconciliationTypeRepository,
   private val financialCalendarRepository: FinancialCalendarRepository,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerJournalEntryValidator::class.java)

   fun validateCreate(dto: GeneralLedgerJournalEntryDTO, company: CompanyEntity): GeneralLedgerJournalEntryDTO {
      logger.trace("Validating create GeneralLedgerJournalEntry {}", dto)

      //todo refactor this
      val (glOpenBegin, glOpenEnd) = financialCalendarRepository.findDateRangeWhenGLIsOpen(company)
      val source = sourceCodeRepository.findOne(dto.source!!.id!!, company)

      doValidation { errors ->
         // GL journal entry validations
         if (dto.entryDate!!.isBefore(glOpenBegin) || dto.entryDate!!.isAfter(glOpenEnd)) {
            errors.add(ValidationError("entryDate", GLNotOpen(dto.entryDate!!)))
         }

         source ?: errors.add(ValidationError("source.id", NotFound(dto.source!!.id!!)))

         if (dto.balance?.compareTo(BigDecimal.ZERO) != 0) {
            errors.add(ValidationError("balance", BalanceMustBeZero(dto.balance!!)))
         }

         // GL journal entry detail validations
         dto.journalEntryDetails.forEach {
            accountRepository.findOne(it.account!!.id!!, company) ?: errors.add(
               ValidationError("journalEntryDetails[index].account.id", NotFound(it.account!!.id!!))
            )
            bankReconciliationTypeRepository.findOne(it.bankType!!.value) ?: errors.add(
               ValidationError("journalEntryDetails[index].bankType.value", NotFound(it.bankType!!.value))
            )
            storeRepository.findOne(it.profitCenter!!.id!!, company) ?: errors.add(
               ValidationError("journalEntryDetails[index].profitCenter.id", NotFound(it.profitCenter!!.id!!))
            )
         }
      }

      return dto
   }
}
