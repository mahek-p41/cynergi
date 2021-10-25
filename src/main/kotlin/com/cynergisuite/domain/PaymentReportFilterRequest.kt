package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.*

@Schema(
   name = "PaymentReportFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [SortableRequestBase::class]
)
class PaymentReportFilterRequest(

   @field:Schema(name = "paymentNumbers", description = "The collection of Payment Numbers to filter results with")
   var pmtNums: Set<String>? = emptySet(),

   @field:Schema(name = "banks", description = "The collection of Bank IDs to filter results with")
   var banks: Set<UUID>? = emptySet(),

   @field:Schema(name = "vendors", description = "The collection of Vendor IDs to filter results with")
   var vendors: Set<UUID>? = emptySet(),

   @field:Schema(name = "vendorGroups", description = "The collection of Vendor Group IDs to filter results with")
   var vendorGroups: Set<UUID>? = emptySet(),

   @field:Schema(name = "beginPmt", description = "Beginning Payment number")
   var beginPmt: String? = null,

   @field:Schema(name = "endPmt", description = "Ending Payment number")
   var endPmt: String? = null,

   @field:Schema(name = "beginBank", description = "Beginning Bank number")
   var beginBank: Int? = null,

   @field:Schema(name = "endBank", description = "Ending Bank number")
   var endBank: Int? = null,

   @field:Schema(name = "beginVendor", description = "Beginning Vendor number")
   var beginVendor: Int? = null,

   @field:Schema(name = "endVendor", description = "Ending Vendor number")
   var endVendor: Int? = null,

   @field:Schema(name = "beginVendorGroup", description = "Beginning Vendor Group value")
   var beginVendorGroup: String? = null,

   @field:Schema(name = "endVendorGroup", description = "Ending Vendor Group value")
   var endVendorGroup: String? = null,

   @field:Schema(name = "status", description = "The Payment Status to filter results with")
   var status: String? = null,

   @field:Schema(name = "type", description = "The Payment Type to filter results with")
   var type: String? = null,

   @field:Schema(name = "includeOption", description = "The include option to filter results with")
   var includeOption: String? = null,

   @field:Schema(name = "frmPmtDt", description = "Beginning payment date")
   var frmPmtDt: OffsetDateTime? = null,

   @field:Schema(name = "thruPmtDt", description = "Ending payment date")
   var thruPmtDt: OffsetDateTime? = null,

   @field:Schema(name = "frmDtClr", description = "Beginning date cleared")
   var frmDtClr: OffsetDateTime? = null,

   @field:Schema(name = "thruDtClr", description = "Ending date cleared")
   var thruDtClr: OffsetDateTime? = null,

   @field:Schema(name = "frmDtVoid", description = "Beginning date voided")
   var frmDtVoid: OffsetDateTime? = null,

   @field:Schema(name = "thruDtVoid", description = "Ending date voided")
   var thruDtVoid: OffsetDateTime? = null,

) : SortableRequestBase<PaymentReportFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "pmtNums" to pmtNums,
         "banks" to banks,
         "vendors" to vendors,
         "vendorGroups" to vendorGroups,
         "beginPmt" to beginPmt,
         "endPmt" to endPmt,
         "beginBank" to beginBank,
         "endBank" to endBank,
         "beginVendor" to beginVendor,
         "endVendor" to endVendor,
         "beginVendorGroup" to beginVendorGroup,
         "endVendorGroup" to endVendorGroup,
         "status" to status,
         "type" to type,
         "includeOption" to includeOption,
         "frmPmtDt" to frmPmtDt,
         "thruPmtDt" to thruPmtDt,
         "frmDtClr" to frmDtClr,
         "thruDtClr" to thruDtClr,
         "frmDtVoid" to frmDtVoid,
         "thruDtVoid" to thruDtVoid,
      )
}
