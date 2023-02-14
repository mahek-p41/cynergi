package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotNull

@Schema(
   name = "AccountPayableInvoiceInquiryFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?account=1&profitCenter=2&fiscalYear=2019",
   allOf = [SortableRequestBase::class]
)
@Introspected
class AccountPayableInvoiceInquiryFilterRequest(

   @field:NotNull
   @field:Schema(name = "vendor", description = "Vendor number")
   var vendor: Int? = null,

   @field:NotNull
   @field:Schema(name = "payTo", description = "Pay to vendor number")
   var payTo: Int? = null,

   @field:Schema(name = "invStatus", description = "Invoice status type value")
   var invStatus: String? = null,

   @field:Schema(name = "poNbr", description = "Purchase order number")
   var poNbr: Int? = null,

   @field:Schema(name = "invNbr", description = "Invoice number")
   var invNbr: String? = null,

   @field:Schema(name = "invDate", description = "Invoice date")
   var invDate: LocalDate? = null,

   @field:Schema(name = "dueDate", description = "Due date")
   var dueDate: LocalDate? = null,

   @field:Schema(name = "invAmount", description = "Invoice amount")
   var invAmount: BigDecimal? = null

) : SortableRequestBase<AccountPayableInvoiceInquiryFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "vendor" to vendor,
         "payTo" to payTo,
         "invStatus" to invStatus,
         "poNbr" to poNbr,
         "invNbr" to invNbr,
         "invDate" to invDate,
         "dueDate" to dueDate,
         "invAmount" to invAmount
      )
}
