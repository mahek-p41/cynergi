package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerJournalValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository,
   private val generalLedgerSourceCodeRepository: GeneralLedgerSourceCodeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerJournalValidator::class.java)

   fun validateCreate(dto: GeneralLedgerJournalDTO, company: Company): GeneralLedgerJournalEntity {
      logger.trace("Validating Save GeneralLedgerJournal {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(dto: GeneralLedgerJournalDTO, company: Company): GeneralLedgerJournalEntity {
      logger.trace("Validating Update GeneralLedgerJournal {}", dto)

      return doSharedValidation(dto, company)
   }

   private fun doSharedValidation(dto: GeneralLedgerJournalDTO, company: Company): GeneralLedgerJournalEntity {
      val account = dto.account?.id?.let { accountRepository.findOne(it, company) }
      val profitCenter = dto.profitCenter?.id?.let { storeRepository.findOne(it, company) }
      val source = dto.source?.id?.let { generalLedgerSourceCodeRepository.findOne(it, company) }

      doValidation { errors ->
         // non-nullable validations
         account ?: errors.add(ValidationError("account.id", NotFound(dto.account!!.id!!)))

         profitCenter ?: errors.add(ValidationError("profitCenter.id", NotFound(dto.profitCenter!!.id!!)))

         // nullable validation
         if (dto.source?.id != null && source == null) {
            errors.add(ValidationError("source.id", NotFound(dto.source!!.id!!)))
         }
      }

      return GeneralLedgerJournalEntity(dto, account!!, profitCenter!!, source!!)
   }
}
