package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountTypeRepository
import com.cynergisuite.middleware.accounting.account.infrastructure.NormalAccountBalanceTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AccountValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val accountTypeRepository: AccountTypeRepository,
   private val balanceTypeRepository: NormalAccountBalanceTypeRepository,
   private val statusTypeRepository: AccountStatusTypeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AccountValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(@Valid accountVO: AccountDTO, company: Company): AccountEntity {
      logger.trace("Validating Save Account {}", accountVO)
      val accountType = accountTypeRepository.findOne(value = accountVO.type.value!!)
      val balanceType = balanceTypeRepository.findOne(value = accountVO.normalAccountBalance.value!!)
      val statusType = statusTypeRepository.findOne(value = accountVO.status.value!!)

      doValidation { errors ->
         accountType ?: errors.add(ValidationError("vo.type.value", NotFound(accountVO.type.value!!)))
         balanceType ?: errors.add(ValidationError("vo.normalAccountBalance.value", NotFound(accountVO.normalAccountBalance.value!!)))
         statusType ?: errors.add(ValidationError("vo.status.value", NotFound(accountVO.status.value!!)))
      }

      return AccountEntity(accountVO, company, accountType!!, balanceType!!, statusType!!)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, accountVO: AccountDTO, company: Company): AccountEntity {
      logger.trace("Validating Update Account {}", accountVO)
      val accountType = accountTypeRepository.findOne(value = accountVO.type.value!!)
      val balanceType = balanceTypeRepository.findOne(value = accountVO.normalAccountBalance.value!!)
      val statusType = statusTypeRepository.findOne(value = accountVO.status.value!!)

      doValidation { errors ->
         accountRepository.findOne(id, company) ?: errors.add(ValidationError("vo.id", NotFound(id)))
         accountType ?: errors.add(ValidationError("vo.type.value", NotFound(accountVO.type.value!!)))
         balanceType ?: errors.add(ValidationError("vo.normalAccountBalance.value", NotFound(accountVO.normalAccountBalance.value!!)))
         statusType ?: errors.add(ValidationError("vo.status.value", NotFound(accountVO.status.value!!)))
      }

      return AccountEntity(accountVO, company, accountType!!, balanceType!!, statusType!!)
   }
}
