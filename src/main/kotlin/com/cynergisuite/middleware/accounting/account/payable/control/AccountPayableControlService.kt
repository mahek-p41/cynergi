package com.cynergisuite.middleware.accounting.account.payable.control

import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.control.infrastructure.AccountPayableControlRepository
import com.cynergisuite.middleware.company.CompanyEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountPayableControlService @Inject constructor(
   private val accountPayableControlValidator: AccountPayableControlValidator,
   private val accountPayableControlRepository: AccountPayableControlRepository
) {
   fun fetchOne(company: CompanyEntity): AccountPayableControlDTO? {
      return accountPayableControlRepository.findOne(company)?.let { transformEntity(it) }
   }

   fun create(dto: AccountPayableControlDTO, company: CompanyEntity): AccountPayableControlDTO {
      val toCreate = accountPayableControlValidator.validateCreate(dto, company)

      return transformEntity(accountPayableControlRepository.insert(toCreate, company))
   }

   fun update(dto: AccountPayableControlDTO, company: CompanyEntity): AccountPayableControlDTO {
      val toUpdate = accountPayableControlValidator.validateUpdate(dto, company)

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
