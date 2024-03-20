package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.store.StoreDTO
import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID
import org.jetbrains.annotations.NotNull

@Introspected
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "AccountPayableInvoiceDistributionDTO", title = "Account Payable Invoice Distribution DTO", description = "Account payable invoice distribution dto")
data class AccountPayableInvoiceDistributionDTO(

   @field:NotNull
   @field:Schema(description = "Account Payable Invoice Distribution ID.", maxLength = 10)
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(name = "invoiceId", description = "Account Payable Invoice ID.", maxLength = 10)
   var invoiceId: UUID? = null,

   @field:NotNull
   @field:Schema(name = "accountId", description = "Account", maxLength = 10)
   var account: AccountDTO? = null,

   @field:NotNull
   @field:Schema(name = "profitCenter", description = "Profit Center", maxLength = 10)
   var profitCenter: StoreDTO? = null,

   @field:NotNull
   @field:Schema(name = "amount", description = "Amount", maxLength = 10)
   var amount: BigDecimal? = null,

)
