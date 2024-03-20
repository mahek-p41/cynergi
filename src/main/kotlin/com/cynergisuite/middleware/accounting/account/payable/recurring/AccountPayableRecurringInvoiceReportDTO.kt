package com.cynergisuite.middleware.accounting.account.payable.recurring

import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.recurring.distribution.infrastructure.AccountPayableRecurringInvoiceDistributionDTO
import com.cynergisuite.middleware.vendor.VendorDTO
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class AccountPayableRecurringInvoiceReportDTO (

   @field:Schema(description = "List of Invoices")
   var id: UUID? = null,
   var vendor: VendorDTO,
   var invoice: String,
   var message: String? = null,
   var amount: BigDecimal,
   var payTo: VendorDTO,
   var status: AccountPayableRecurringInvoiceStatusTypeDTO,
   val separateCheckIndicator: Boolean,
   val dueDate: LocalDate? = null,
   val lastCreatedInPeriod: LocalDate? = null,
   val lastTransferToCreateInvoiceDate: LocalDate? = null,
   val nextCreationDate: LocalDate? = null,
   val nextInvoiceDate: LocalDate? = null,
   val nextExpenseDate: LocalDate? = null,
   val startDate: LocalDate? = null,
   val endDate: LocalDate? = null,
   val distributions: List<AccountPayableRecurringInvoiceDistributionDTO> = mutableListOf()
   )
