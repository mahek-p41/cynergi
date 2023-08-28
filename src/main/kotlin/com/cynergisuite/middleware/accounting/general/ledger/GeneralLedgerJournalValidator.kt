package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarService
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerJournalRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.BalanceMustBeZero
import com.cynergisuite.middleware.localization.DateMustBeAfterPreviousFiscalYear
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class GeneralLedgerJournalValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val generalLedgerJournalRepository: GeneralLedgerJournalRepository,
   private val generalLedgerSourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val storeRepository: StoreRepository,
   private val financialCalendarService: FinancialCalendarService
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerJournalValidator::class.java)

   fun validateCreate(dto: GeneralLedgerJournalDTO, company: CompanyEntity): GeneralLedgerJournalEntity {
      logger.trace("Validating Save GeneralLedgerJournal {}", dto)

      return doSharedValidation(dto, company, null)
   }

   fun validateUpdate(id: UUID, dto: GeneralLedgerJournalDTO, company: CompanyEntity): GeneralLedgerJournalEntity {
      logger.trace("Validating Update GeneralLedgerJournal {}", dto)

      val existingGeneralLedgerJournal = generalLedgerJournalRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doSharedValidation(dto, company, existingGeneralLedgerJournal)
   }

   private fun doSharedValidation(
      dto: GeneralLedgerJournalDTO,
      company: CompanyEntity,
      existingGeneralLedgerJournal: GeneralLedgerJournalEntity?
   ): GeneralLedgerJournalEntity {
      val account = dto.account?.id?.let { accountRepository.findOne(it, company) }
      val profitCenter = dto.profitCenter?.id?.let { storeRepository.findOne(it, company) }
      val source = dto.source?.id?.let { generalLedgerSourceCodeRepository.findOne(it, company) }
      val wholeCal = financialCalendarService.fetchFiscalYears(company)

      doValidation { errors ->
         // non-nullable validations
         account ?: errors.add(ValidationError("account.id", NotFound(dto.account!!.id!!)))

         profitCenter ?: errors.add(ValidationError("profitCenter.id", NotFound(dto.profitCenter!!.id!!)))

         // nullable validation
         if (dto.source?.id != null && source == null) {
            errors.add(ValidationError("source.id", NotFound(dto.source!!.id!!)))
         }

         if (dto.date!! < wholeCal[1].end) {
            errors.add(ValidationError("entryDate", DateMustBeAfterPreviousFiscalYear(dto.date!!)))
         }

      }

      return GeneralLedgerJournalEntity(
         existingGeneralLedgerJournal?.id,
         dto,
         account!!,
         profitCenter!!,
         source!!
      )
   }

   fun validateTransfer(totals: GeneralLedgerPendingJournalCountDTO): Boolean {
      doValidation { errors ->
         if (totals.balance!!.toDouble() != 0.00) {
            errors.add(ValidationError("totals.balance", BalanceMustBeZero(totals.balance!!)))
         }
      }
      return true
   }
}
