package com.cynergisuite.middleware.accounting.general.ledger.end.month

import com.cynergisuite.domain.ExpenseReportFilterRequest
import com.cynergisuite.util.APInvoiceReportOverviewType
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
   beginAcct: Int?,
   endAcct: Int?,
   beginVen: Int?,
   endVen: Int?,
   beginVenGr: String?,
   endVenGr: String?,
   beginDate: LocalDate,
   endDate: LocalDate,
   iclHoldInv: Boolean?,
   invStatus: List<String>?,
   overviewType: APInvoiceReportOverviewType?,
   sortBy: String?,
   @field:Schema(name = "jeDate", description = "Journal Entry Date")
   val jeDate: LocalDate?,
) : ExpenseReportFilterRequest(
   beginAcct,
   endAcct,
   beginVen,
   endVen,
   beginVenGr,
   endVenGr,
   beginDate,
   endDate,
   iclHoldInv ?: true,
   invStatus ?: listOf("O", "P"),
   overviewType ?: APInvoiceReportOverviewType.DETAILED,
   sortBy ?: "account",
) {
   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      super.myToStringValues() + listOf("jeDate" to jeDate)
}
