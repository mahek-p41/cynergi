package com.cynergisuite.middleware.accounting.general.ledger.journal.entry

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure.BankReconciliationTypeRepository
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.GLNotOpen
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.ProfitCenterMustMatchBankProfitCenter
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class GeneralLedgerJournalEntryValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val bankReconciliationTypeRepository: BankReconciliationTypeRepository,
   private val bankRepository: BankRepository,
   private val financialCalendarRepository: FinancialCalendarRepository,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerJournalEntryValidator::class.java)

   fun validateCreate(dto: GeneralLedgerJournalEntryDTO, company: CompanyEntity): GeneralLedgerJournalEntryDTO {
      logger.trace("Validating create GeneralLedgerJournalEntry {}", dto)

      val (glOpenBegin, glOpenEnd) = financialCalendarRepository.findDateRangeWhenGLIsOpen(company)
      val source = sourceCodeRepository.findOne(dto.source!!.id!!, company)

      doValidation { errors ->
         // GL journal entry validations
         if (dto.entryDate!!.isBefore(glOpenBegin) || dto.entryDate!!.isAfter(glOpenEnd)) {
            errors.add(ValidationError("entryDate", GLNotOpen(dto.entryDate!!)))
         }

         source ?: errors.add(ValidationError("source.id", NotFound(dto.source!!.id!!)))

         // GL journal entry detail validations
         dto.journalEntryDetails.forEach {
            val account = accountRepository.findOne(it.account!!.id!!, company)
            val profitCenter = storeRepository.findOne(it.profitCenter!!.id!!, company)

            account ?: errors.add(
               ValidationError("journalEntryDetails[index].account.id", NotFound(account!!.id!!))
            )

            profitCenter ?: errors.add(
               ValidationError("journalEntryDetails[index].profitCenter.id", NotFound(profitCenter!!.id))
            )

            // account is bank account validations
            if (it.bankType != null) {
               val bank = account.bankId?.let { bankId -> bankRepository.findOne(bankId, company) }

               bankReconciliationTypeRepository.findOne(it.bankType!!.value) ?: errors.add(
                  ValidationError("journalEntryDetails[index].bankType.value", NotFound(it.bankType!!.value))
               )

               if (profitCenter.id != bank?.generalLedgerProfitCenter?.myId()) {
                  errors.add(ValidationError("journalEntryDetails[index].profitCenter", ProfitCenterMustMatchBankProfitCenter(profitCenter)))
               }
            }
         }
      }

      return dto
   }
}
