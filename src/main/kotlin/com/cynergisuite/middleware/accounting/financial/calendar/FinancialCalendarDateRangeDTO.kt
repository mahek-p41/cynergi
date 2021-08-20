package com.cynergisuite.middleware.accounting.financial.calendar

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "FinancialCalendarDateRange", title = "An entity with a date range")
data class FinancialCalendarDateRangeDTO(

   @field:NotNull
   @field:Schema(description = "Period from date")
   var periodFrom: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Period to date")
   var periodTo: LocalDate? = null

) {
   constructor(entity: FinancialCalendarDateRangeEntity) :
      this(
         periodFrom = entity.periodFrom,
         periodTo = entity.periodTo
      )
}
