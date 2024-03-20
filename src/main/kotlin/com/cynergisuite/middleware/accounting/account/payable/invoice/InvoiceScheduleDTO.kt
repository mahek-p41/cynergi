package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableInvoiceSchedule", title = "Account Payable Invoice Schedule", description = "Account payable invoice schedule")
data class InvoiceScheduleDTO(

   @field:NotNull
   @field:Size(max = 20)
   @field:Schema(description = "Vendor Payment Term ID")
   var vendorPaymentTermId: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Invoice Date")
   var invoiceDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Invoice Amount")
   var invoiceAmount: BigDecimal? = null
)
