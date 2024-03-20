package com.cynergisuite.middleware.accounting.account.payable.expense

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "AccountPayableInvoiceOverviewDTO", title = "Account Payable Invoice Overview", description = "Account Payable Invoice Overview")
data class AccountPayableInvoiceOverviewDTO(

   @field:Schema(description = "Account Payable Expense ID")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Vendor number")
   var vendorNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Vendor name")
   var vendorName: String? = null,

   @field:NotNull
   @field:Schema(description = "Vendor group")
   var vendorGroup: String? = null,

   @field:NotNull
   @field:Schema(description = "Expense")
   var invoice: String? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice type value")
   var type: String? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice date")
   var invoiceDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice status id")
   var status: String? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice amount")
   var invoiceAmount: BigDecimal? = null,

   @field:Schema(description = "Account payable invoice expense date", required = false)
   var expenseDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice paid amount")
   var paidAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Bank number")
   var bankNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Payment number")
   var pmtNumber: String? = null,

   @field:NotNull
   @field:Schema(description = "Payment date")
   var pmtDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Payment voided date")
   var dateVoided: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice message")
   var notes: String? = null,

   private var periodDateRangeDTO: ClosedRange<LocalDate>,

   ) : Identifiable {
   constructor(invoiceDTO: AccountPayableExpenseReportDTO, vendorNumber: ClosedRange<LocalDate>) : this(
      id = invoiceDTO.myId(),
      vendorNumber = invoiceDTO.vendorNumber,
      vendorName = invoiceDTO.vendorName,
      vendorGroup = invoiceDTO.vendorGroup,
      invoice = invoiceDTO.invoice,
      type = invoiceDTO.type,
      invoiceDate = invoiceDTO.invoiceDate,
      status = invoiceDTO.status,
      invoiceAmount = invoiceDTO.invoiceAmount,
      expenseDate = invoiceDTO.expenseDate,
      paidAmount = invoiceDTO.paidAmount,
      bankNumber = invoiceDTO.bankNumber,
      pmtNumber = invoiceDTO.pmtNumber,
      pmtDate = invoiceDTO.pmtDate,
      dateVoided = invoiceDTO.dateVoided,
      notes = invoiceDTO.notes,
      periodDateRangeDTO = vendorNumber
   )

   override fun myId(): UUID? = id

   val beginDate = periodDateRangeDTO.start
   val endDate = periodDateRangeDTO.endInclusive

   val beginBalance: BigDecimal =
      if (expenseDate!! < beginDate &&
         (status != "P" || pmtDate?.let { it > endDate } ?: true)) invoiceAmount!!
      else BigDecimal.ZERO

   val newInvoiceAmount: BigDecimal = if (expenseDate!! in periodDateRangeDTO) invoiceAmount!! else BigDecimal.ZERO

   val paidInvoiceAmount: BigDecimal = if (pmtDate != null && pmtDate!! in periodDateRangeDTO) paidAmount!! else BigDecimal.ZERO

   val endBalance = beginBalance.plus(newInvoiceAmount).minus(paidInvoiceAmount)

}
