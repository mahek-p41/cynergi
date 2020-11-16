package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountTypeRepository
import com.cynergisuite.middleware.accounting.account.infrastructure.NormalAccountBalanceTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val accountTypeRepository: AccountTypeRepository,
   private val balanceTypeRepository: NormalAccountBalanceTypeRepository,
   private val statusTypeRepository: AccountStatusTypeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AccountValidator::class.java)

   fun validateCreate(accountDTO: AccountDTO, company: Company): AccountEntity {
      logger.trace("Validating Save Account {}", accountDTO)
      val accountType = accountTypeRepository.findOne(value = accountDTO.type?.value!!)
      val balanceType = balanceTypeRepository.findOne(value = accountDTO.normalAccountBalance?.value!!)
      val statusType = statusTypeRepository.findOne(value = accountDTO.status?.value!!)
      val existingAccount = accountRepository.findByNumber(number = accountDTO.number!!, company = company)

      doValidation { errors ->
         accountType ?: errors.add(ValidationError("type.value", NotFound(accountDTO.type?.value!!)))
         balanceType ?: errors.add(ValidationError("normalAccountBalance.value", NotFound(accountDTO.normalAccountBalance?.value!!)))
         statusType ?: errors.add(ValidationError("status.value", NotFound(accountDTO.status?.value!!)))
         if (existingAccount != null) errors.add(ValidationError("number", Duplicate(accountDTO.number!!)))
      }

      return AccountEntity(accountDTO, company, accountType!!, balanceType!!, statusType!!)
   }

   fun validateUpdate(id: Long, accountDTO: AccountDTO, company: Company): AccountEntity {
      logger.trace("Validating Update Account {}", accountDTO)
      val accountType = accountTypeRepository.findOne(value = accountDTO.type?.value!!)
      val balanceType = balanceTypeRepository.findOne(value = accountDTO.normalAccountBalance?.value!!)
      val statusType = statusTypeRepository.findOne(value = accountDTO.status?.value!!)
      val existingAccount = accountRepository.findByNumber(number = accountDTO.number!!, company = company)

      doValidation { errors ->
         accountRepository.findOne(id, company) ?: errors.add(ValidationError("vo.id", NotFound(id)))
         accountType ?: errors.add(ValidationError("type.value", NotFound(accountDTO.type?.value!!)))
         balanceType ?: errors.add(ValidationError("normalAccountBalance.value", NotFound(accountDTO.normalAccountBalance?.value!!)))
         statusType ?: errors.add(ValidationError("status.value", NotFound(accountDTO.status?.value!!)))
         if (existingAccount != null && existingAccount.id != accountDTO.id) errors.add(ValidationError("number", Duplicate(accountDTO.number!!)))
      }

      return AccountEntity(accountDTO, company, accountType!!, balanceType!!, statusType!!)
   }
}
