package com.cynergisuite.middleware.accounting.account.payable.control

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorType
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorType
import com.cynergisuite.middleware.accounting.account.payable.control.infrastructure.AccountPayableControlRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.PrintCurrencyIndicatorTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.PurchaseOrderNumberRequiredIndicatorTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AccountPayableControlValidator @Inject constructor(
   private val accountPayableControlRepository: AccountPayableControlRepository,
   private val accountRepository: AccountRepository,
   private val printCurrencyIndicatorTypeRepository: PrintCurrencyIndicatorTypeRepository,
   private val purchaseOrderNumberRequiredIndicatorTypeRepository: PurchaseOrderNumberRequiredIndicatorTypeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableControlValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(@Valid dto: AccountPayableControlDTO, company: Company): AccountPayableControlEntity {
      logger.debug("Validating Create AccountPayableControl {}", dto)

      val printCurrencyIndicatorType = printCurrencyIndicatorTypeRepository.findOne(dto.printCurrencyIndicatorType!!.value)!!
      val purchaseOrderNumberRequiredIndicatorType = purchaseOrderNumberRequiredIndicatorTypeRepository.findOne(dto.purchaseOrderNumberRequiredIndicatorType!!.value)!!
      val generalLedgerInventoryClearingAccount = accountRepository.findOne(dto.generalLedgerInventoryClearingAccount!!.id!!, company)
      val generalLedgerInventoryAccount = accountRepository.findOne(dto.generalLedgerInventoryAccount!!.id!!, company)

      doValidation { errors ->
         if (accountPayableControlRepository.exists(company)) {
            errors.add(ValidationError("company", Duplicate("Account payable control for user's company " + company.myDataset())))
         }

         doSharedValidation(errors, dto, printCurrencyIndicatorType, purchaseOrderNumberRequiredIndicatorType, generalLedgerInventoryClearingAccount, generalLedgerInventoryAccount)
      }

      return AccountPayableControlEntity(
         dto,
         printCurrencyIndicatorType = printCurrencyIndicatorType,
         purchaseOrderNumberRequiredIndicatorType = purchaseOrderNumberRequiredIndicatorType,
         generalLedgerInventoryClearingAccount = generalLedgerInventoryClearingAccount!!,
         generalLedgerInventoryAccount = generalLedgerInventoryAccount!!
      )
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, dto: AccountPayableControlDTO, company: Company): AccountPayableControlEntity {
      logger.debug("Validating Update AccountPayableControl {}", dto)

      val printCurrencyIndicatorType = printCurrencyIndicatorTypeRepository.findOne(dto.printCurrencyIndicatorType!!.value)
      val purchaseOrderNumberRequiredIndicatorType = purchaseOrderNumberRequiredIndicatorTypeRepository.findOne(dto.purchaseOrderNumberRequiredIndicatorType!!.value)
      val generalLedgerInventoryClearingAccount = accountRepository.findOne(dto.generalLedgerInventoryClearingAccount!!.id!!, company)
      val generalLedgerInventoryAccount = accountRepository.findOne(dto.generalLedgerInventoryAccount!!.id!!, company)

      doValidation { errors ->
         if (!accountPayableControlRepository.exists(id)) {
            errors.add(ValidationError("id", NotFound(id)))
         }

         doSharedValidation(errors, dto, printCurrencyIndicatorType, purchaseOrderNumberRequiredIndicatorType, generalLedgerInventoryClearingAccount, generalLedgerInventoryAccount)
      }

      return AccountPayableControlEntity(
         dto,
         printCurrencyIndicatorType = printCurrencyIndicatorType!!,
         purchaseOrderNumberRequiredIndicatorType = purchaseOrderNumberRequiredIndicatorType!!,
         generalLedgerInventoryClearingAccount = generalLedgerInventoryClearingAccount!!,
         generalLedgerInventoryAccount = generalLedgerInventoryAccount!!
      )
   }

   private fun doSharedValidation(
      errors: MutableSet<ValidationError>,
      dto: AccountPayableControlDTO,
      printCurrencyIndicatorType: PrintCurrencyIndicatorType?,
      purchaseOrderNumberRequiredIndicatorType: PurchaseOrderNumberRequiredIndicatorType?,
      generalLedgerInventoryClearingAccount: AccountEntity?,
      generalLedgerInventoryAccount: AccountEntity?
   ) {
      printCurrencyIndicatorType
         ?: errors.add(ValidationError("printCurrencyIndicatorType.value", NotFound(dto.printCurrencyIndicatorType!!.value)))

      purchaseOrderNumberRequiredIndicatorType
         ?: errors.add(ValidationError("purchaseOrderNumberRequiredIndicatorType.value", NotFound(dto.purchaseOrderNumberRequiredIndicatorType!!.value)))

      generalLedgerInventoryClearingAccount
         ?: errors.add(ValidationError("generalLedgerInventoryClearingAccount.id", NotFound(dto.generalLedgerInventoryClearingAccount!!.id!!)))

      generalLedgerInventoryAccount
         ?: errors.add(ValidationError("generalLedgerInventoryAccount.id", NotFound(dto.generalLedgerInventoryAccount!!.id!!)))
   }
}
