package com.cynergisuite.middleware.accounting.account.payable.control

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorType
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorType

data class AccountPayableControlEntity(
   val id: Long? = null,
   val payAfterDiscountDate: Boolean,
   val resetExpense: Boolean,
   val useRebatesIndicator: Boolean,
   val tradeCompanyIndicator: Boolean,
   val printCurrencyIndicatorType: PrintCurrencyIndicatorType,
   val lockInventoryIndicator: Boolean,
   val purchaseOrderNumberRequiredIndicatorType: PurchaseOrderNumberRequiredIndicatorType
   ) : Identifiable {

   constructor(dto: AccountPayableControlDTO, printCurrencyIndicatorType: PrintCurrencyIndicatorType, purchaseOrderNumberRequiredIndicatorType: PurchaseOrderNumberRequiredIndicatorType) :
      this(
         id = dto.id,
         payAfterDiscountDate = dto.payAfterDiscountDate!!,
         resetExpense = dto.resetExpense!!,
         useRebatesIndicator = dto.useRebatesIndicator!!,
         tradeCompanyIndicator = dto.tradeCompanyIndicator!!,
         printCurrencyIndicatorType = printCurrencyIndicatorType,
         lockInventoryIndicator = dto.lockInventoryIndicator!!,
         purchaseOrderNumberRequiredIndicatorType = purchaseOrderNumberRequiredIndicatorType
      )

   override fun myId(): Long? = id
}
