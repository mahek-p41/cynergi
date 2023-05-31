package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "AccountPayableCheckPreviewInvoiceDTO", title = "Account Payable Check Preview Invoice DTO", description = "Account Payable Check Preview Invoice DTO")
data class AccountPayableCheckPreviewInvoiceEntity(

   var id: UUID,
   var invoiceNumber: String,
   var date: LocalDate,
   var dueDate: LocalDate,
   var poNumber: UUID? = null,
   var gross: BigDecimal,
   var discount: BigDecimal,
   var deduction: BigDecimal? = BigDecimal.ZERO,
   var netPaid: BigDecimal? = BigDecimal.ZERO,
   var notes: String? = null
) {

}
