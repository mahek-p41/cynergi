package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentTypeTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "AccountPayableInvoiceScheduleDTO", title = "Account Payable Invoice Schedule", description = "Account payable invoice schedule")
data class AccountPayableInvoiceScheduleDTO(

   @field:Schema(description = "Account payable invoice id")
   var invoiceId: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Vendor id")
   var companyId: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Schedule Date")
   var scheduleDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Payment Sequence Number")
   var paymentSequenceNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Amount to Pay")
   var amountToPay: BigDecimal? = null,

   @field:Schema(description = "Bank")
   var bank: UUID? = null,

   @field:Schema(description = "External Payment Type ID")
   var externalPaymentTypeId: AccountPayablePaymentTypeTypeDTO? = null,

   @field:Schema(description = "External Payment Number")
   var externalPaymentNumber: String? = null,

   @field:Schema(description = "External Payment Date")
   var externalPaymentDate: LocalDate? = null,

   @field:Schema(description = "Selected For Processing")
   var selectedForProcessing: Boolean? = null,

   @field:Schema(description = "Payment Processed")
   var paymentProcessed: Boolean? = null,

   ) {
   constructor(entity: AccountPayableInvoiceScheduleEntity) :
      this(
         invoiceId = entity.invoiceId,
         companyId = entity.companyId,
         scheduleDate = entity.scheduleDate,
         paymentSequenceNumber = entity.paymentSequenceNumber,
         amountToPay = entity.amountToPay,
         bank = entity.bank,
         externalPaymentTypeId = entity.externalPaymentTypeId?.let { AccountPayablePaymentTypeTypeDTO(it) },
         externalPaymentNumber = entity.externalPaymentNumber,
         externalPaymentDate = entity.externalPaymentDate,
         selectedForProcessing = entity.selectedForProcessing,
         paymentProcessed = entity.paymentProcessed
      )
}
