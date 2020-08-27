package com.cynergisuite.middleware.accounting.account.payable.control

import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.control.infrastructure.AccountPayableControlRepository
import com.cynergisuite.middleware.company.Company
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AccountPayableControlService @Inject constructor(
   private val accountPayableControlValidator: AccountPayableControlValidator,
   private val accountPayableControlRepository: AccountPayableControlRepository
) {
   fun fetchOne(company: Company): AccountPayableControlDTO? {
      return accountPayableControlRepository.findOne(company)?.let { transformEntity(it) }
   }

   @Validated
   fun create(@Valid dto: AccountPayableControlDTO, company: Company): AccountPayableControlDTO {
      val toCreate = accountPayableControlValidator.validateCreate(dto, company)

      return transformEntity(accountPayableControlRepository.insert(toCreate, company))
   }

   @Validated
   fun update(id: Long, @Valid dto: AccountPayableControlDTO, company: Company): AccountPayableControlDTO {
      val toUpdate = accountPayableControlValidator.validateUpdate(id, dto, company)

      return transformEntity(accountPayableControlRepository.update(toUpdate, company))
   }

   private fun transformEntity(accountPayableControl: AccountPayableControlEntity): AccountPayableControlDTO {
      return AccountPayableControlDTO(
         entity = accountPayableControl,
         checkFormType = AccountPayableCheckFormTypeDTO(accountPayableControl.checkFormType),
         printCurrencyIndicatorType = PrintCurrencyIndicatorTypeDTO(accountPayableControl.printCurrencyIndicatorType),
         purchaseOrderNumberRequiredIndicatorType = PurchaseOrderNumberRequiredIndicatorTypeDTO(accountPayableControl.purchaseOrderNumberRequiredIndicatorType)
      )
   }
}
