package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(
   name = "AccountPayableListPaymentsFilterRequest",
   title = "Account Payable List Payments Filter Request",
   description = "Filter request for Account Payable Payments Listing",
   allOf = [PageRequestBase::class]
)
@Introspected
class AccountPayableListPaymentsFilterRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "beginBank", description = "Beginning Bank number")
   var beginBank: Int? = null,

   @field:Schema(name = "beginPmt", description = "Beginning Payment number")
   var beginPmt: String? = null,

   @field:Schema(name = "type", description = "The Payment Type to filter results with: All, ACH, or Check")
   var type: String? = null,

   @field:Schema(name = "frmPmtDt", description = "Beginning payment date")
   var frmPmtDt: OffsetDateTime? = null

) : PageRequestBase<AccountPayableListPaymentsFilterRequest>(page, size, sortBy, sortDirection) {

   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): AccountPayableListPaymentsFilterRequest =
      AccountPayableListPaymentsFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         beginBank = this.beginBank,
         beginPmt = this.beginPmt,
         type = this.type,
         frmPmtDt = this.frmPmtDt
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "beginBank" to beginBank,
         "beginPmt" to beginPmt,
         "type" to type,
         "frmPmtDt" to frmPmtDt
      )
}
