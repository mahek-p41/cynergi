package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.Pattern

@Schema(
   name = "InvoiceReportFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [SortableRequestBase::class]
)
@Introspected
class InvoiceReportFilterRequest(

   @field:Schema(name = "beginVen", description = "Beginning Vendor number")
   var beginVen: Int? = null,

   @field:Schema(name = "endVen", description = "Ending Vendor number")
   var endVen: Int? = null,

   @field:Schema(name = "beginOpr", description = "Beginning Operator number")
   var beginOpr: Int? = null,

   @field:Schema(name = "endOpr", description = "Ending Vendor number")
   var endOpr: Int? = null,

   @field:Schema(name = "beginPO", description = "Beginning PO number")
   var beginPO: Int? = null,

   @field:Schema(name = "endPO", description = "Ending PO number")
   var endPO: Int? = null,

   @field:Schema(name = "beginInvDate", description = "Beginning Invoice date")
   var beginInvDate: LocalDate? = null,

   @field:Schema(name = "endInvDate", description = "Ending Invoice date")
   var endInvDate: LocalDate? = null,

   @field:Schema(name = "beginExpDate", description = "Beginning expense date")
   var beginExpDate: LocalDate? = null,

   @field:Schema(name = "endExpDate", description = "Ending expense date")
   var endExpDate: LocalDate? = null,

   @field:Schema(name = "beginEnDate", description = "Beginning entry date")
   var beginEnDate: LocalDate? = null,

   @field:Schema(name = "endEnDate", description = "Ending entry date")
   var endEnDate: LocalDate? = null,

   @field:Schema(name = "beginPaidDate", description = "Beginning paid date")
   var beginPaidDate: LocalDate? = null,

   @field:Schema(name = "endPaidDate", description = "Ending paid date")
   var endPaidDate: LocalDate? = null,

   @field:Schema(name = "beginDueDate", description = "Beginning due date")
   var beginDueDate: LocalDate? = null,

   @field:Schema(name = "endDueDate", description = "Ending due date")
   var endDueDate: LocalDate? = null,

   @field:Schema(name = "beginVenGr", description = "Beginning Vendor group value")
   var beginVenGr: String? = null,

   @field:Schema(name = "endVenGr", description = "Ending Vendor group value")
   var endVenGr: String? = null,

   @field:Schema(name = "invStatus", description = "The Invoice Status to filter results with")
   var invStatus: String? = null,

   @field:Schema(name = "useTax", description = "Only use tax indicator")
   var useTax: Boolean? = null,

   @field:Pattern(regexp = "poHeader.number|apInvoice.invoice|vendor.number|vendor.name")
   @field:Schema(description = "The column to sort the purchase order invoice report by (poHeader.number|apInvoice.invoice|vendor.number|vendor.name).", defaultValue = "poHeader.number")
   override var sortBy: String? = null,

) : SortableRequestBase<InvoiceReportFilterRequest>("poHeader.number", "ASC") {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "beginVen" to beginVen,
         "endVen" to endVen,
         "beginOpr" to beginOpr,
         "endOpr" to endOpr,
         "beginPO" to beginPO,
         "endPO" to endPO,
         "beginInvDate" to beginInvDate,
         "endInvDate" to endInvDate,
         "beginExpDate" to beginExpDate,
         "endExpDate" to endExpDate,
         "beginEnDate" to beginEnDate,
         "endEnDate" to endEnDate,
         "beginPaidDate" to beginPaidDate,
         "endPaidDate" to endPaidDate,
         "beginDueDate" to beginDueDate,
         "endDueDate" to endDueDate,
         "beginVenGr" to beginVenGr,
         "endVenGr" to endVenGr,
         "invStatus" to invStatus,
         "useTax" to useTax,
      )
}
