package com.cynergisuite.middleware.accounting.account.payable.control

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormType
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorType
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorType
import java.util.UUID

data class AccountPayableControlEntity(
   val id: UUID? = null,
   val checkFormType: AccountPayableCheckFormType,
   val payAfterDiscountDate: Boolean,
   val resetExpense: Boolean,
   val useRebatesIndicator: Boolean,
   val tradeCompanyIndicator: Boolean,
   val printCurrencyIndicatorType: PrintCurrencyIndicatorType,
   val lockInventoryIndicator: Boolean,
   val purchaseOrderNumberRequiredIndicatorType: PurchaseOrderNumberRequiredIndicatorType,
   val generalLedgerInventoryClearingAccount: AccountEntity,
   val generalLedgerInventoryAccount: AccountEntity
) : Identifiable {

   constructor(
      id: UUID?,
      dto: AccountPayableControlDTO,
      checkFormType: AccountPayableCheckFormType,
      printCurrencyIndicatorType: PrintCurrencyIndicatorType,
      purchaseOrderNumberRequiredIndicatorType: PurchaseOrderNumberRequiredIndicatorType,
      generalLedgerInventoryClearingAccount: AccountEntity,
      generalLedgerInventoryAccount: AccountEntity
   ) :
      this(
         id = id,
         checkFormType = checkFormType,
         payAfterDiscountDate = dto.payAfterDiscountDate!!,
         resetExpense = dto.resetExpense!!,
         useRebatesIndicator = dto.useRebatesIndicator!!,
         tradeCompanyIndicator = dto.tradeCompanyIndicator!!,
         printCurrencyIndicatorType = printCurrencyIndicatorType,
         lockInventoryIndicator = dto.lockInventoryIndicator!!,
         purchaseOrderNumberRequiredIndicatorType = purchaseOrderNumberRequiredIndicatorType,
         generalLedgerInventoryClearingAccount = generalLedgerInventoryClearingAccount,
         generalLedgerInventoryAccount = generalLedgerInventoryAccount
      )

   override fun myId(): UUID? = id
}
