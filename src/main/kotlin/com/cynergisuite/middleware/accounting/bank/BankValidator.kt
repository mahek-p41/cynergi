package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.AccountInUse
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class BankValidator @Inject constructor(
   private val bankRepository: BankRepository,
   private val storeRepository: StoreRepository,
   private val accountRepository: AccountRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(BankValidator::class.java)

   fun validateCreate(bankDTO: BankDTO, company: CompanyEntity): BankEntity {
      logger.trace("Validating Save Bank {}", bankDTO)
      val existingBankByNumber = bankRepository.findByNumber(bankDTO.number!!, company)

      return doValidation(existingBankByNumber = existingBankByNumber, bankDTO = bankDTO, company = company)
   }

   fun validateUpdate(id: UUID, bankDTO: BankDTO, company: CompanyEntity): BankEntity {
      logger.trace("Validating Update Bank {}", bankDTO)

      val existingBank = bankRepository.findOne(id, company) ?: throw NotFoundException(id)
      val existingBankByNumber = bankRepository.findByNumber(bankDTO.number!!, company)

      return doValidation(existingBank = existingBank, existingBankByNumber = existingBankByNumber, bankDTO = bankDTO, company = company)
   }

   private fun doValidation(existingBank: BankEntity? = null, existingBankByNumber: BankEntity? = null, bankDTO: BankDTO, company: CompanyEntity): BankEntity {
      val generalProfitCenter = storeRepository.findOne(bankDTO.generalLedgerProfitCenter!!.id!!, company)
      val generalLedgerAccount = accountRepository.findOne(bankDTO.generalLedgerAccount!!.id!!, company)

      doValidation { errors ->
         if (existingBank == null && existingBankByNumber != null) errors.add(ValidationError("number", Duplicate(bankDTO.number!!)))
         if (existingBank != null && existingBankByNumber != null && existingBankByNumber.id != existingBank.id) errors.add(ValidationError("number", Duplicate(bankDTO.number!!)))
         generalProfitCenter ?: errors.add(ValidationError("generalLedgerProfitCenter.id", NotFound(bankDTO.generalLedgerProfitCenter!!.id!!)))
         if (generalLedgerAccount == null) {
            errors.add(ValidationError("generalLedgerAccount.id", NotFound(bankDTO.generalLedgerAccount!!.id!!)))
         } else if (existingBank == null && generalLedgerAccount.bankId != null) {
            errors.add(ValidationError("generalLedgerAccount.id", AccountInUse()))
         } else if (existingBank != null && existingBank.generalLedgerAccount.id != generalLedgerAccount.id && generalLedgerAccount.bankId != null) {
            errors.add(ValidationError("generalLedgerAccount.id", AccountInUse()))
         }

      }

      return BankEntity(
         bankDTO = bankDTO,
         store = generalProfitCenter!!,
         account = generalLedgerAccount!!
      )
   }
}