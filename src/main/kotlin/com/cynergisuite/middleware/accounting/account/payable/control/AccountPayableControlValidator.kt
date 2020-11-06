package com.cynergisuite.middleware.accounting.account.payable.control

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.payable.control.infrastructure.AccountPayableControlRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableCheckFormTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.PrintCurrencyIndicatorTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.PurchaseOrderNumberRequiredIndicatorTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.ConfigAlreadyExist
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountPayableControlValidator @Inject constructor(
   private val accountPayableControlRepository: AccountPayableControlRepository,
   private val accountRepository: AccountRepository,
   private val accountPayableCheckFormTypeRepository: AccountPayableCheckFormTypeRepository,
   private val printCurrencyIndicatorTypeRepository: PrintCurrencyIndicatorTypeRepository,
   private val purchaseOrderNumberRequiredIndicatorTypeRepository: PurchaseOrderNumberRequiredIndicatorTypeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableControlValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(dto: AccountPayableControlDTO, company: Company): AccountPayableControlEntity {
      logger.debug("Validating Create AccountPayableControl {}", dto)

      return doSharedValidation(dto, company)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(dto: AccountPayableControlDTO, company: Company): AccountPayableControlEntity {
      logger.debug("Validating Update AccountPayableControl {}", dto)

      val accountPayableControlEntity = accountPayableControlRepository.findOne(company) ?: throw NotFoundException(company.myId()!!)

      return doSharedValidation(dto, company, accountPayableControlEntity)
   }

   private fun doSharedValidation(
      dto: AccountPayableControlDTO,
      company: Company,
      entity: AccountPayableControlEntity? = null
   ): AccountPayableControlEntity {
      val checkFormType = accountPayableCheckFormTypeRepository.findOne(dto.checkFormType!!.value)
      val printCurrencyIndicatorType = printCurrencyIndicatorTypeRepository.findOne(dto.printCurrencyIndicatorType!!.value)
      val purchaseOrderNumberRequiredIndicatorType = purchaseOrderNumberRequiredIndicatorTypeRepository.findOne(dto.purchaseOrderNumberRequiredIndicatorType!!.value)
      val generalLedgerInventoryClearingAccount = accountRepository.findOne(dto.generalLedgerInventoryClearingAccount!!.id!!, company)
      val generalLedgerInventoryAccount = accountRepository.findOne(dto.generalLedgerInventoryAccount!!.id!!, company)

      doValidation { errors ->
         if (accountPayableControlRepository.exists(company) && entity == null) { // tried to create an Account Payable Control record, but one already existed, basically they did a post when they should have done a put.
            errors.add(ValidationError("company", ConfigAlreadyExist(company.myDataset())))
         }
         checkFormType
            ?: errors.add(ValidationError("checkFormType.value", NotFound(dto.checkFormType!!.value)))

         printCurrencyIndicatorType
            ?: errors.add(ValidationError("printCurrencyIndicatorType.value", NotFound(dto.printCurrencyIndicatorType!!.value)))

         purchaseOrderNumberRequiredIndicatorType
            ?: errors.add(ValidationError("purchaseOrderNumberRequiredIndicatorType.value", NotFound(dto.purchaseOrderNumberRequiredIndicatorType!!.value)))

         generalLedgerInventoryClearingAccount
            ?: errors.add(ValidationError("generalLedgerInventoryClearingAccount.id", NotFound(dto.generalLedgerInventoryClearingAccount!!.id!!)))

         generalLedgerInventoryAccount
            ?: errors.add(ValidationError("generalLedgerInventoryAccount.id", NotFound(dto.generalLedgerInventoryAccount!!.id!!)))
      }

      return AccountPayableControlEntity(
         dto,
         checkFormType = checkFormType!!,
         printCurrencyIndicatorType = printCurrencyIndicatorType!!,
         purchaseOrderNumberRequiredIndicatorType = purchaseOrderNumberRequiredIndicatorType!!,
         generalLedgerInventoryClearingAccount = generalLedgerInventoryClearingAccount!!,
         generalLedgerInventoryAccount = generalLedgerInventoryAccount!!
      )
   }
}
