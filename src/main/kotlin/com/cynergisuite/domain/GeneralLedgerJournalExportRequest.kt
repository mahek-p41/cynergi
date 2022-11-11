package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.LocalDate

@Schema(
   name = "GeneralLedgerJournalExportRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [SortableRequestBase::class]
)
@Introspected
class GeneralLedgerJournalExportRequest(

   @field:Schema(name = "profitCenter", description = "General ledger profit center")
   var profitCenter: Int? = null,

   @field:Schema(name = "beginSourceCode", description = "Beginning General ledger source code")
   var beginSourceCode: String? = null,

   @field:Schema(name = "endSourceCode", description = "Ending General ledger source code")
   var endSourceCode: String? = null,

   @field:Schema(name = "entryDate", description = "From date for general ledger journal")
   var fromDate: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date for general ledger journal")
   var thruDate: LocalDate? = null,

//   cynergi clients only use csv format
//   @field:Schema(name = "fileFormat", description = "General ledger source code export file format")
//   var fileFormat: Int? = 1 or 2,

   ) : SortableRequestBase<GeneralLedgerJournalExportRequest>(null, null) {

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy()

   override fun equals(other: Any?): Boolean =
      if (other is GeneralLedgerJournalExportRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.profitCenter, other.profitCenter)
            .append(this.beginSourceCode, other.beginSourceCode)
            .append(this.endSourceCode, other.endSourceCode)
            .append(this.fromDate, other.fromDate)
            .append(this.thruDate, other.thruDate)
//            .append(this.fileFormat, other.fileFormat)
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
//         .append(this.fileFormat)
         .toHashCode()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "profitCenter" to profitCenter,
         "beginSourceCode" to beginSourceCode,
         "endSourceCode" to endSourceCode,
         "fromDate" to fromDate,
         "thruDate" to thruDate,
//         "fileFormat" to fileFormat,
      )
}
