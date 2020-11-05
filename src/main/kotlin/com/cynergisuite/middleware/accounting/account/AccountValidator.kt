package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountTypeRepository
import com.cynergisuite.middleware.accounting.account.infrastructure.NormalAccountBalanceTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.ValidationError
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

      doValidation { errors ->
         accountType ?: errors.add(ValidationError("vo.type.value", NotFound(accountDTO.type?.value!!)))
         balanceType ?: errors.add(ValidationError("vo.normalAccountBalance.value", NotFound(accountDTO.normalAccountBalance?.value!!)))
         statusType ?: errors.add(ValidationError("vo.status.value", NotFound(accountDTO.status?.value!!)))
      }

      return AccountEntity(accountDTO, company, accountType!!, balanceType!!, statusType!!)
   }

   fun validateUpdate(id: Long, accountDTO: AccountDTO, company: Company): AccountEntity {
      logger.trace("Validating Update Account {}", accountDTO)
      val accountType = accountTypeRepository.findOne(value = accountDTO.type?.value!!)
      val balanceType = balanceTypeRepository.findOne(value = accountDTO.normalAccountBalance?.value!!)
      val statusType = statusTypeRepository.findOne(value = accountDTO.status?.value!!)

      doValidation { errors ->
         accountRepository.findOne(id, company) ?: errors.add(ValidationError("vo.id", NotFound(id)))
         accountType ?: errors.add(ValidationError("vo.type.value", NotFound(accountDTO.type?.value!!)))
         balanceType ?: errors.add(ValidationError("vo.normalAccountBalance.value", NotFound(accountDTO.normalAccountBalance?.value!!)))
         statusType ?: errors.add(ValidationError("vo.status.value", NotFound(accountDTO.status?.value!!)))
      }

      return AccountEntity(accountDTO, company, accountType!!, balanceType!!, statusType!!)
   }
}
