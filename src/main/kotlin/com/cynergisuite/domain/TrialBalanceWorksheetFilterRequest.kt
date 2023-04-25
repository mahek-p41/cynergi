package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.LocalDate

@Schema(
   name = "TrialBalanceWorksheetFilterReport",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [PageRequestBase::class]
)
class TrialBalanceWorksheetFilterRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "beginProfitCenter", description = "Beginning General ledger profit center")
   var beginAccount: Int? = null,

   @field:Schema(name = "endProfitCenter", description = "Ending General ledger profit center")
   var endAccount: Int? = null,

   @field:Schema(name = "profitCenter", description = "Beginning General ledger profit center")
   var profitCenter: Int? = null,

   @field:Schema(name = "entryDate", description = "From date for general ledger journal")
   var fromDate: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date for general ledger journal")
   var thruDate: LocalDate? = null,


) : PageRequestBase<TrialBalanceWorksheetFilterRequest>(page, size, sortBy, sortDirection) {

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy()

   override fun equals(other: Any?): Boolean =
      if (other is TrialBalanceWorksheetFilterRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.beginAccount, other.beginAccount)
            .append(this.endAccount, other.endAccount)
            .append(this.profitCenter, other.profitCenter)
            .append(this.fromDate, other.fromDate)
            .append(this.thruDate, other.thruDate)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.beginAccount)
         .append(this.endAccount)
         .append(this.profitCenter)
         .append(this.fromDate)
         .append(this.thruDate)
         .toHashCode()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): TrialBalanceWorksheetFilterRequest =
      TrialBalanceWorksheetFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         beginAccount = this.beginAccount,
         endAccount = this.endAccount,
         profitCenter = this.profitCenter,
         fromDate = this.fromDate,
         thruDate = this.thruDate
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "beginAccount" to beginAccount,
         "endAccount" to endAccount,
         "profitCenter" to profitCenter,
         "fromDate" to fromDate,
         "thruDate" to thruDate
      )
}
