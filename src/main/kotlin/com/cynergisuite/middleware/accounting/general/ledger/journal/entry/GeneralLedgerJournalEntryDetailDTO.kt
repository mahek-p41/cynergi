package com.cynergisuite.middleware.accounting.general.ledger.journal.entry

import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.store.StoreDTO
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "GeneralLedgerJournalEntryDetail", title = "General Ledger Journal Entry Detail", description = "General ledger journal entry detail")
data class GeneralLedgerJournalEntryDetailDTO(

   @field:NotNull
   @field:Schema(name = "account", description = "Account")
   var account: AccountDTO? = null,

   @field:Schema(name = "bankType", description = "Bank reconciliation type", required = false)
   var bankType: BankReconciliationTypeDTO? = null,

   @field:NotNull
   @field:Schema(name = "profitCenter", description = "Profit center")
   var profitCenter: StoreDTO? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "amount", description = "Amount")
   var amount: BigDecimal? = null

)