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
   var sourceCode: String? = null,

   @field:Schema(name = "startingDate", description = "From date for general ledger journal")
   var startingDate: LocalDate? = null,

   @field:Schema(name = "endingDate", description = "Thru date for general ledger journal")
   var endingDate: LocalDate? = null,

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
            .append(this.sourceCode, other.sourceCode)
            .append(this.startingDate, other.startingDate)
            .append(this.endingDate, other.endingDate)
//            .append(this.fileFormat, other.fileFormat)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.profitCenter)
         .append(this.sourceCode)
         .append(this.startingDate)
         .append(this.endingDate)
//         .append(this.fileFormat)
         .toHashCode()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "profitCenter" to profitCenter,
         "sourceCode" to sourceCode,
         "startingDate" to startingDate,
         "endingDate" to endingDate,
//         "fileFormat" to fileFormat,
      )
}
