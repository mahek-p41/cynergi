package com.cynergisuite.domain

import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationType
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.LocalDate

@Schema(
   name = "BankReconFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [PageRequestBase::class]
)
class BankReconFilterRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "beginBank", description = "Beginning Bank Number")
   var beginBank: Int? = null,

   @field:Schema(name = "endBank", description = "End Bank Number")
   var endBank: Int? = null,

   @field:Schema(name = "entryDate", description = "From date for bank reconciliation")
   var fromDate: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date for bank reconciliation")
   var thruDate: LocalDate? = null,

   @field:Schema(name = "beginDocument", description = "Beginning Document Number")
   var beginDocument: Int? = null,

   @field:Schema(name = "endDocument", description = "End Document Number")
   var endDocument: Int? = null,

   @field:Schema(name = "type", description = "Bank Reconciliation Type")
   var type: BankReconciliationType? = null,

   @field:Schema(name = "description", description = "Filter full or partial description for bank reconciliation")
   var description: Boolean = false,

   @field:Schema(name = "status", description = "Bank Reconciliation Status")
   var status: String? = null,

   @field:Schema(name = "begingClearDate", description = "From clear date for bank reconciliation")
   var beginClearDate: LocalDate? = null,

   @field:Schema(name = "endClearDate", description = "Thru clear date for bank reconciliation")
   var endClearDate: LocalDate? = null,

   @field:Schema(name = "layout", description = "Vendor Name or Vendor Number")
   var layout: String? = null,

) : PageRequestBase<BankReconFilterRequest>(page, size, sortBy, sortDirection) {

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy()

   override fun equals(other: Any?): Boolean =
      if (other is BankReconFilterRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.beginBank, other.beginBank)
            .append(this.endBank, other.endBank)
            .append(this.fromDate, other.fromDate)
            .append(this.thruDate, other.thruDate)
            .append(this.beginDocument, other.beginDocument)
            .append(this.endDocument, other.endDocument)
            .append(this.type , other.type)
            .append(this.description, other.description)
            .append(this.status, other.status)
            .append(this.beginClearDate, other.beginClearDate)
            .append(this.endClearDate, other.endClearDate)
            .append(this.layout, other.layout)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.beginBank)
         .append(this.endBank)
         .append(this.fromDate)
         .append(this.thruDate)
         .append(this.beginDocument)
         .append(this.endDocument)
         .append(this.type)
         .append(this.description)
         .append(this.status)
         .append(this.beginClearDate)
         .append(this.endClearDate)
         .append(this.layout)
         .toHashCode()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): BankReconFilterRequest =
      BankReconFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         beginBank = this.beginBank,
         endBank = this.endBank,
         fromDate = this.fromDate,
         thruDate = this.thruDate,
         beginDocument = this.beginDocument,
         endDocument = this.endDocument,
         type = this.type,
         description = this.description,
         status = this.status,
         beginClearDate = this.beginClearDate,
         endClearDate = this.endClearDate,
         layout = this.layout
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "beginBank" to beginBank,
         "endBank" to endBank,
         "fromDate" to fromDate,
         "thruDate" to thruDate,
         "beginDocument" to beginDocument,
         "endDocument" to endDocument,
         "type" to type,
         "description" to description,
         "status" to status,
         "beginClearDate" to beginClearDate,
         "endClearDate" to endClearDate,
         "layout" to layout
      )
}
