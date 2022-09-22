package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.LocalDate

@Schema(
   name = "GeneralLedgerJournalFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [PageRequestBase::class]
)
class GeneralLedgerJournalFilterRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "profitCenter", description = "General ledger profit center")
   var profitCenter: Int? = null,

   @field:Schema(name = "beginSourceCode", description = "Beginning General ledger source code")
   var beginSourceCode: String? = null,

   @field:Schema(name = "endSourceCode", description = "End General ledger source code")
   var endSourceCode: String? = null,

   @field:Schema(name = "entryDate", description = "From date for general ledger journal")
   var fromDate: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date for general ledger journal")
   var thruDate: LocalDate? = null,

   ) : PageRequestBase<GeneralLedgerJournalFilterRequest>(page, size, sortBy, sortDirection) {

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy()

   override fun equals(other: Any?): Boolean =
      if (other is GeneralLedgerJournalFilterRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.profitCenter, other.profitCenter)
            .append(this.beginSourceCode, other.beginSourceCode)
            .append(this.endSourceCode, other.endSourceCode)
            .append(this.fromDate, other.fromDate)
            .append(this.thruDate, other.thruDate)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.profitCenter)
         .append(this.beginSourceCode)
         .append(this.endSourceCode)
         .append(this.fromDate)
         .append(this.thruDate)
         .toHashCode()

   protected override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): GeneralLedgerJournalFilterRequest =
      GeneralLedgerJournalFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         profitCenter = this.profitCenter,
         beginSourceCode = this.beginSourceCode,
         endSourceCode = this.endSourceCode,
         fromDate = this.fromDate,
         thruDate = this.thruDate,
      )

   protected override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "profitCenter" to profitCenter,
         "beginSourceCode" to beginSourceCode,
         "endSourceCode" to endSourceCode,
         "fromDate" to fromDate,
         "thruDate" to thruDate,
      )
}
