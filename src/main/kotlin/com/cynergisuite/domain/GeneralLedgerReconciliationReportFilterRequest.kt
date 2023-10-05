package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import javax.validation.constraints.NotNull

@Schema(
   name = "GeneralLedgerReconciliationReportFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?date=01-01-2019",
   allOf = [SortableRequestBase::class]
)
class GeneralLedgerReconciliationReportFilterRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:NotNull
   @field:Schema(name = "date", description = "Date")
   var date: LocalDate? = null

) : PageRequestBase<GeneralLedgerReconciliationReportFilterRequest>(page, size, sortBy, sortDirection) {

   @ValidPageSortBy("number")
   override fun sortByMe(): String = sortBy()
   override fun equals(other: Any?): Boolean =
      if (other is GeneralLedgerReconciliationReportFilterRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.date, other.date)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.date)
         .toHashCode()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): GeneralLedgerReconciliationReportFilterRequest =
      GeneralLedgerReconciliationReportFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         date = this.date,
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "date" to date
      )
}

