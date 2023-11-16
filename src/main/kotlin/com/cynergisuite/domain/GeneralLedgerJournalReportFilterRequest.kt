package com.cynergisuite.domain

import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerReportSortEnum
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.LocalDate

@Schema(
   name = "GeneralLedgerJournalFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P&posted=true",
   allOf = [PageRequestBase::class]
)
class GeneralLedgerJournalReportFilterRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "beginProfitCenter", description = "Beginning General ledger profit center")
   var beginProfitCenter: Int? = null,

   @field:Schema(name = "endProfitCenter", description = "Ending General ledger profit center")
   var endProfitCenter: Int? = null,

   @field:Schema(name = "beginSourceCode", description = "Beginning General ledger source code")
   var beginSourceCode: String? = null,

   @field:Schema(name = "endSourceCode", description = "End General ledger source code")
   var endSourceCode: String? = null,

   @field:Schema(name = "entryDate", description = "From date for general ledger journal")
   var fromDate: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date for general ledger journal")
   var thruDate: LocalDate? = null,

   @field:Schema(name = "subtotal", description = "Subtotal breaks for general ledger journal")
   var subtotal: Boolean = false,

   @field:Schema(name = "sortOption", description = "Sort option for general ledger journal")
   var sortOption: GeneralLedgerReportSortEnum? = null,

   @field:Schema(name = "description", description = "List description for general ledger journals")
   var description: Boolean = false,

   @field:Schema(name = "posted", description = "Fetch posted journals", defaultValue = "false")
   var posted: Boolean = false,

   ) : PageRequestBase<GeneralLedgerJournalReportFilterRequest>(page, size, sortBy, sortDirection) {

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy()

   override fun equals(other: Any?): Boolean =
      if (other is GeneralLedgerJournalReportFilterRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.beginProfitCenter, other.beginProfitCenter)
            .append(this.endProfitCenter, other.endProfitCenter)
            .append(this.beginSourceCode, other.beginSourceCode)
            .append(this.endSourceCode, other.endSourceCode)
            .append(this.fromDate, other.fromDate)
            .append(this.thruDate, other.thruDate)
            .append(this.subtotal, other.subtotal)
            .append(this.sortOption, other.sortOption)
            .append(this.description, other.description)
            .append(this.posted, other.posted)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.beginProfitCenter)
         .append(this.endProfitCenter)
         .append(this.beginSourceCode)
         .append(this.endSourceCode)
         .append(this.fromDate)
         .append(this.thruDate)
         .append(this.subtotal)
         .append(this.sortOption)
         .append(this.description)
         .append(this.posted)
         .toHashCode()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): GeneralLedgerJournalReportFilterRequest =
      GeneralLedgerJournalReportFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         beginProfitCenter = this.beginProfitCenter,
         endProfitCenter = this.endProfitCenter,
         beginSourceCode = this.beginSourceCode,
         endSourceCode = this.endSourceCode,
         fromDate = this.fromDate,
         thruDate = this.thruDate,
         subtotal = this.subtotal,
         sortOption = this.sortOption,
         description = this.description,
         posted = this.posted
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "beginProfitCenter" to beginProfitCenter,
         "endProfitCenter" to endProfitCenter,
         "beginSourceCode" to beginSourceCode,
         "endSourceCode" to endSourceCode,
         "fromDate" to fromDate,
         "thruDate" to thruDate,
         "subtotal" to subtotal,
         "sortOption" to sortOption,
         "description" to description,
         "posted" to posted
      )
}
