package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.Pattern

@Introspected
@Schema(
   name = "ReconcileBankAccountFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [PageRequestBase::class]
)
class ReconcileBankAccountFilterRequest(
   @field:Schema(name = "bank", description = "Bank Number")
   var bank: Long,

   @field:Schema(name = "date", description = "Date for bank reconciliation")
   @field:Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}\$")
   var date: LocalDate,

) : SortableRequestBase<ReconcileBankAccountFilterRequest>("bank_number", "ASC") {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any>> =
      listOf(
         "bank" to bank,
         "date" to date,
      )
}
