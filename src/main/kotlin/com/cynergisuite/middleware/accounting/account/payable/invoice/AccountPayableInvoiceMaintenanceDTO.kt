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
   var apInvoice: AccountPayableInvoiceDTO? = null,

   @field:NotNull
   var apPayment: AccountPayablePaymentDTO? = null,

   @field:NotNull
   var glDistribution: AccountPayableDistributionTemplateDTO? = null,

   @field:NotNull
   var apInvoiceSchedule: AccountPayableInvoiceScheduleDTO? = null
)
