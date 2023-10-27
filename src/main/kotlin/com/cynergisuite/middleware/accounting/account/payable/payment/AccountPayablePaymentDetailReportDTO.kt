package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayablePaymentDetailReport", title = "Account Payable Payment Detail Report", description = "Account Payable Payment Detail Report")
data class AccountPayablePaymentDetailReportDTO(

   @field:Schema(description = "Account Payable Payment Detail id")
   var id: UUID? = null,

   @field:Schema(description = "Vendor number")
   var vendorNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice")
   var invoice: String? = null,

   @field:NotNull
   @field:Schema(description = "Invoice amount")
   var invoiceAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice date")
   var invoiceDate: LocalDate? = null

) : Identifiable {
   constructor(entity: AccountPayablePaymentDetailEntity) :
      this(
         id = entity.id,
         vendorNumber = entity.vendor!!.number,
         invoice = entity.invoice.invoice,
         invoiceAmount = entity.amount,
         invoiceDate = entity.invoice.invoiceDate
      )

   override fun myId(): UUID? = id
}
