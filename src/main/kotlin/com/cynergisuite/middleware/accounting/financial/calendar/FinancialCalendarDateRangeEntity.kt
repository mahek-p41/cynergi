package com.cynergisuite.middleware.accounting.financial.calendar

import java.time.LocalDate

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
