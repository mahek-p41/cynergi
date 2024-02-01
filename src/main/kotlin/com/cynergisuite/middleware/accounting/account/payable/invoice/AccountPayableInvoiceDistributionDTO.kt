package com.cynergisuite.middleware.accounting.account.payable.invoice

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
   var id: UUID? = null,

   @field:NotNull
   var invoiceId: UUID? = null,

   @field:NotNull
   var accountId: UUID? = null,

   @field:NotNull
   var profitCenter: Long? = null,

   @field:NotNull
   var amount: BigDecimal? = null,
)
