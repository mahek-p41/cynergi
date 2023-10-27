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

   @field:Schema(name = "fromDate", description = "From date for bank reconciliation")
   var fromDate: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date for bank reconciliation")
   var thruDate: LocalDate? = null,

   @field:Schema(name = "beginDocument", description = "Beginning Document Number")
   var beginDocument: String? = null,

   @field:Schema(name = "endDocument", description = "End Document Number")
   var endDocument: String? = null,

   @field:Schema(name = "bankType", description = "Bank Reconciliation Type Value")
   var bankType: String? = null,

   @field:Schema(name = "description", description = "Filter full or partial description for bank reconciliation")
   var description: String? = null,

   @field:Schema(name = "status", description = "Bank Reconciliation Status")
   var status: String? = null,

   @field:Schema(name = "beginClearDate", description = "From clear date for bank reconciliation")
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
            .append(this.bankType , other.bankType)
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
         .append(this.bankType)
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
         bankType = this.bankType,
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
         "bankType" to bankType,
         "description" to description,
         "status" to status,
         "beginClearDate" to beginClearDate,
         "endClearDate" to endClearDate,
         "layout" to layout
      )
}
