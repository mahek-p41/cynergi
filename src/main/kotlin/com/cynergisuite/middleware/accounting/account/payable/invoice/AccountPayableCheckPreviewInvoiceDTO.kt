package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "AccountPayableCheckPreviewInvoiceDTO", title = "Account Payable Check Preview Invoice DTO", description = "Account Payable Check Preview Invoice DTO")
data class AccountPayableCheckPreviewInvoiceDTO(

   @field:NotNull
   @field:Schema(description = "Invoice ID")
   var id: UUID,

   @field:NotNull
   @field:Schema(description = "Invoice Number")
   var invoiceNumber: String,

   @field:NotNull
   @field:Schema(description = "Invoice Date")
   var date: LocalDate,

   @field:NotNull
   @field:Schema(description = "Invoice Due Date")
   var dueDate: LocalDate,

   @field:Schema(description = "PO Number")
   var poNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Gross amount")
   var gross: BigDecimal,

   @field:NotNull
   @field:Schema(description = "Discount")
   var discount: BigDecimal,

   @field:NotNull
   @field:Schema(description = "Deduction")
   var deduction: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Net Paid")
   var netPaid: BigDecimal? = BigDecimal.ZERO,

   @field:Schema(description = "Notes")
   var notes: String? = null
) {
   constructor(entity: AccountPayableCheckPreviewInvoiceEntity) :
      this(
         id = entity.id,
         invoiceNumber = entity.invoiceNumber,
         date = entity.date,
         dueDate = entity.dueDate,
         poNumber = entity.poNumber,
         gross = entity.gross,
         discount = entity.discount,
         deduction = entity.deduction,
         netPaid = entity.netPaid,
         notes = entity.notes
      )
}
