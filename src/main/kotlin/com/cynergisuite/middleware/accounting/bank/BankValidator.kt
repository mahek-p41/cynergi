package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankCurrencyTypeRepository
import com.cynergisuite.middleware.address.AddressRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class BankValidator @Inject constructor(
   private val addressRepository: AddressRepository,
   private val bankRepository: BankRepository,
   private val storeRepository: StoreRepository,
   private val accountRepository: AccountRepository,
   private val bankCurrencyTypeRepository: BankCurrencyTypeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(BankValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(@Valid bankDTO: BankDTO, company: Company): BankEntity {
      logger.trace("Validating Save Bank {}", bankDTO)
      val generalProfitCenter = storeRepository.findOne(bankDTO.generalLedgerProfitCenter!!.id!!, company)
      val generalLedgerAccount = accountRepository.findOne(bankDTO.generalLedgerAccount!!.id!!, company)
      val currencyType = bankCurrencyTypeRepository.findOne(value = bankDTO.currency!!.value!!)

      doValidation { errors ->
         doSharedValidation(generalProfitCenter, errors, bankDTO, generalLedgerAccount, currencyType)
      }

      return BankEntity(bankDTO, company, generalProfitCenter!!, generalLedgerAccount!!, currencyType!!)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, bankDTO: BankDTO, company: Company): BankEntity {
      logger.trace("Validating Update Bank {}", bankDTO)
      val generalProfitCenter = storeRepository.findOne(bankDTO.generalLedgerProfitCenter!!.id!!, company)
      val generalLedgerAccount = accountRepository.findOne(bankDTO.generalLedgerAccount!!.id!!, company)
      val currencyType = bankCurrencyTypeRepository.findOne(value = bankDTO.currency!!.value!!)

      doValidation { errors ->
         if (!bankRepository.exists(id)) {
            errors.add(ValidationError("id", NotFound(id)))
         }

         if (!addressRepository.exists(bankDTO.address!!.id!!)) {
            errors.add(ValidationError("address.id", NotFound(bankDTO.address!!.id!!)))
         }

         doSharedValidation(generalProfitCenter, errors, bankDTO, generalLedgerAccount, currencyType)
      }

      return BankEntity(bankDTO, company, generalProfitCenter!!, generalLedgerAccount!!, currencyType!!)
   }

   private fun doSharedValidation(generalProfitCenter: StoreEntity?, errors: MutableSet<ValidationError>, bankDTO: BankDTO, generalLedgerAccount: AccountEntity?, currencyType: BankCurrencyType?) {
      generalProfitCenter ?: errors.add(ValidationError("generalLedgerProfitCenter.id", NotFound(bankDTO.generalLedgerProfitCenter!!.id!!)))
      generalLedgerAccount?: errors.add(ValidationError("generalLedgerAccount.id", NotFound(bankDTO.generalLedgerAccount!!.id!!)))
      currencyType ?: errors.add(ValidationError("currency.value", NotFound(bankDTO.currency!!.value!!)))
   }
}
