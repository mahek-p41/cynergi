package com.cynergisuite.middleware.accounting.financial.calendar

import io.micronaut.core.annotation.Introspected
import java.time.LocalDate

@Introspected
data class FinancialCalendarDateRangeEntity(
   val periodFrom: LocalDate,
   val periodTo: LocalDate
) {

   constructor(dto: FinancialCalendarDateRangeDTO) :
      this(
         periodFrom = dto.periodFrom!!,
         periodTo = dto.periodTo!!
      )
}
