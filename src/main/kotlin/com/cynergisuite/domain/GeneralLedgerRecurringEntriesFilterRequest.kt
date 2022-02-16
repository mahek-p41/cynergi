package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.OffsetDateTime

@Schema(
   name = "GeneralLedgerRecurringEntriesFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [PageRequestBase::class]
)
class GeneralLedgerRecurringEntriesFilterRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "entryType", description = "General ledger recurring type (for transfer or report)")
   var entryType: String? = null,

   @field:Schema(name = "sourceCode", description = "General ledger source code (for transfer or report)")
   var sourceCode: String? = null,

   @field:Schema(name = "entryDate", description = "Entry date must fall in general ledger recurring date range (for transfer only)")
   var entryDate: OffsetDateTime? = null,

   @field:Schema(name = "employeeNumber", description = "Employee number of user executing the general ledger recurring transfer (for transfer only)")
   var employeeNumber: Int? = null,

) : PageRequestBase<GeneralLedgerRecurringEntriesFilterRequest>(page, size, sortBy, sortDirection) {

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy()

   override fun equals(other: Any?): Boolean =
      if (other is GeneralLedgerRecurringEntriesFilterRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.entryType, other.entryType)
            .append(this.sourceCode, other.sourceCode)
            .append(this.entryDate, other.entryDate)
            .append(this.employeeNumber, other.employeeNumber)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.entryType)
         .append(this.sourceCode)
         .append(this.entryDate)
         .append(this.employeeNumber)
         .toHashCode()

   protected override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): GeneralLedgerRecurringEntriesFilterRequest =
      GeneralLedgerRecurringEntriesFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         entryType = this.entryType,
         sourceCode = this.sourceCode,
         entryDate = this.entryDate,
         employeeNumber = this.employeeNumber,
      )

   protected override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "entryType" to entryType,
         "sourceCode" to sourceCode,
         "entryDate" to entryDate,
         "employeeNumber" to employeeNumber,
      )
}
