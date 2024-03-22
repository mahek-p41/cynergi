package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionTemplateDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDTO
import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import org.jetbrains.annotations.NotNull

@Introspected
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "AccountPayableInvoiceMaintenanceDto", title = "Account Payable Invoice Maintenance DTO", description = "Account payable invoice maintenance dto")
data class AccountPayableInvoiceMaintenanceDTO(

   @field:NotNull
   @field:Schema(name = "apInvoice", description = "Account Payable Invoice.", maxLength = 10)
   var apInvoice: AccountPayableInvoiceDTO? = null,

   @field:NotNull
   @field:Schema(name = "apPayment", description = "Account Payable Payment.", maxLength = 10)
   var apPayment: AccountPayablePaymentDTO? = null,

   @field:NotNull
   @field:Schema(name = "glDistribution", description = "Account Payable Distribution Template.", maxLength = 10)
   var glDistribution: AccountPayableDistributionTemplateDTO? = null,

   @field:NotNull
   @field:Schema(name = "apInvoiceSchedule", description = "Account Payable Invoice Schedule.", maxLength = 10)
   var apInvoiceSchedule: MutableList<AccountPayableInvoiceScheduleDTO>? = null
)