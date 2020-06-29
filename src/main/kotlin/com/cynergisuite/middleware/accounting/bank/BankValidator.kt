package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class BankValidator @Inject constructor(
   private val bankRepository: BankRepository,
   private val storeRepository: StoreRepository,
   private val accountRepository: AccountRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(BankValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(@Valid bankDTO: BankDTO, company: Company): BankEntity {
      logger.trace("Validating Save Bank {}", bankDTO)

      return doValidation(bankDTO = bankDTO, company = company)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, bankDTO: BankDTO, company: Company): BankEntity {
      logger.trace("Validating Update Bank {}", bankDTO)

      val existingBank = bankRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doValidation(existingBank, bankDTO, company)
   }

   private fun doValidation(existingBank: BankEntity? = null, bankDTO: BankDTO, company: Company): BankEntity {
      val generalProfitCenter = storeRepository.findOne(bankDTO.generalLedgerProfitCenter!!.id!!, company)
      val generalLedgerAccount = accountRepository.findOne(bankDTO.generalLedgerAccount!!.id!!, company)

      doValidation { errors ->
         if (generalProfitCenter == null) {
            errors.add(ValidationError("generalLedgerProfitCenter.id", NotFound(bankDTO.generalLedgerProfitCenter!!.id!!)))
         }

         if (generalLedgerAccount == null) {
            errors.add(ValidationError("generalLedgerAccount.id", NotFound(bankDTO.generalLedgerAccount!!.id!!)))
         }
      }

      return if (existingBank != null) {
         BankEntity(
            id = existingBank.id,
            bankDTO = bankDTO,
            company = company,
            store = generalProfitCenter!!,
            account = generalLedgerAccount!!
         )
      } else {
         BankEntity(
            bankDTO = bankDTO,
            company = company,
            store = generalProfitCenter!!,
            account = generalLedgerAccount!!
         )
      }
   }
}
