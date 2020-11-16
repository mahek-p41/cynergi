package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankValidator @Inject constructor(
   private val bankRepository: BankRepository,
   private val storeRepository: StoreRepository,
   private val accountRepository: AccountRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(BankValidator::class.java)

   fun validateCreate(bankDTO: BankDTO, company: Company): BankEntity {
      logger.trace("Validating Save Bank {}", bankDTO)
      val existingBankByNumber = bankRepository.findByNumber(bankDTO.number!!, company)

      return doValidation(existingBankByNumber = existingBankByNumber, bankDTO = bankDTO, company = company)
   }

   fun validateUpdate(id: Long, bankDTO: BankDTO, company: Company): BankEntity {
      logger.trace("Validating Update Bank {}", bankDTO)

      val existingBank = bankRepository.findOne(id, company) ?: throw NotFoundException(id)
      val existingBankByNumber = bankRepository.findByNumber(bankDTO.number!!, company)

      return doValidation(existingBank = existingBank, existingBankByNumber = existingBankByNumber, bankDTO = bankDTO, company = company)
   }

   private fun doValidation(existingBank: BankEntity? = null, existingBankByNumber: BankEntity? = null, bankDTO: BankDTO, company: Company): BankEntity {
      val generalProfitCenter = storeRepository.findOne(bankDTO.generalLedgerProfitCenter!!.id!!, company)
      val generalLedgerAccount = accountRepository.findOne(bankDTO.generalLedgerAccount!!.id!!, company)

      doValidation { errors ->
         if (existingBank == null && existingBankByNumber != null) errors.add(ValidationError("number", Duplicate(bankDTO.number!!)))
         if (existingBank != null && existingBankByNumber != null && existingBankByNumber.id != existingBank.id) errors.add(ValidationError("number", Duplicate(bankDTO.number!!)))
         generalProfitCenter ?: errors.add(ValidationError("generalLedgerProfitCenter.id", NotFound(bankDTO.generalLedgerProfitCenter!!.id!!)))
         generalLedgerAccount ?: errors.add(ValidationError("generalLedgerAccount.id", NotFound(bankDTO.generalLedgerAccount!!.id!!)))
      }

      return BankEntity(
         bankDTO = bankDTO,
         company = company,
         store = generalProfitCenter!!,
         account = generalLedgerAccount!!
      )
   }
}
