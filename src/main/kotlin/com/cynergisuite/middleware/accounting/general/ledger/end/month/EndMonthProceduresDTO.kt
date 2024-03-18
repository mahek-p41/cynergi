package com.cynergisuite.middleware.accounting.general.ledger.end.month

import com.cynergisuite.domain.ExpenseReportFilterRequest
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(
   name = "EndMonthProceduresDTO",
   title = "End Month Procedures DTO",
   description = "Defines the parameters for End Month Procedures"
)
@Introspected
class EndMonthProceduresDTO(
    beginAcct: Int? = null,
    endAcct: Int? = null,
    beginVen: Int? = null,
    endVen: Int? = null,
    beginVenGr: String? = null,
    endVenGr: String? = null,
    beginDate: LocalDate? = null,
    endDate: LocalDate? = null,
    iclHoldInv: Boolean? = false,
    invStatus: List<String>? = null,
    sortBy: String? = "account",
    @field:Schema(name = "jeDate", description = "Journal Entry Date")
    val jeDate: LocalDate? = null
) : ExpenseReportFilterRequest(
   beginAcct,
   endAcct,
   beginVen,
   endVen,
   beginVenGr,
   endVenGr,
   beginDate,
   endDate,
   iclHoldInv,
   invStatus,
   sortBy
) {
   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      super.myToStringValues() + listOf("jeDate" to jeDate)
}
