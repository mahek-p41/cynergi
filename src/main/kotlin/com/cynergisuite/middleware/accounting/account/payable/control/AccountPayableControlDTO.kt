package com.cynergisuite.middleware.accounting.account.payable.control

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableControl", title = "Account payable control", description = "Account payable control entity")
data class AccountPayableControlDTO(

   @field:Schema(description = "Account payable control id")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Account payable check form type")
   var checkFormType: AccountPayableCheckFormTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "Pay after discount date")
   var payAfterDiscountDate: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Reset expense")
   var resetExpense: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Use rebates indicator")
   var useRebatesIndicator: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Trade company indicator")
   var tradeCompanyIndicator: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Print currency indicator type")
   var printCurrencyIndicatorType: PrintCurrencyIndicatorTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "Lock inventory indicator")
   var lockInventoryIndicator: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Purchase order number required indicator type")
   var purchaseOrderNumberRequiredIndicatorType: PurchaseOrderNumberRequiredIndicatorTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "General ledger inventory clearing account")
   var generalLedgerInventoryClearingAccount: AccountDTO? = null,

   @field:NotNull
   @field:Schema(description = "General ledger inventory account")
   var generalLedgerInventoryAccount: AccountDTO? = null

) : Identifiable {
   constructor(
      entity: AccountPayableControlEntity,
      checkFormType: AccountPayableCheckFormTypeDTO,
      printCurrencyIndicatorType: PrintCurrencyIndicatorTypeDTO,
      purchaseOrderNumberRequiredIndicatorType: PurchaseOrderNumberRequiredIndicatorTypeDTO
   ) :
      this(
         id = entity.id,
         checkFormType = checkFormType,
         payAfterDiscountDate = entity.payAfterDiscountDate,
         resetExpense = entity.resetExpense,
         useRebatesIndicator = entity.useRebatesIndicator,
         tradeCompanyIndicator = entity.tradeCompanyIndicator,
         printCurrencyIndicatorType = printCurrencyIndicatorType,
         lockInventoryIndicator = entity.lockInventoryIndicator,
         purchaseOrderNumberRequiredIndicatorType = purchaseOrderNumberRequiredIndicatorType,
         generalLedgerInventoryClearingAccount = entity.generalLedgerInventoryClearingAccount.let { AccountDTO(it) },
         generalLedgerInventoryAccount = entity.generalLedgerInventoryAccount.let { AccountDTO(it) }
      )

   override fun myId(): UUID? = id
}
