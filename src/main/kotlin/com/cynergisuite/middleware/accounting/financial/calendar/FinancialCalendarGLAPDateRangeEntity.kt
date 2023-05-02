package com.cynergisuite.middleware.accounting.financial.calendar

import io.micronaut.core.annotation.Introspected
import java.time.LocalDate
@Introspected
data class FinancialCalendarGLAPDateRangeEntity(
   val glPeriodFrom: LocalDate,
   val glPeriodTo: LocalDate,
   val apPeriodFrom: LocalDate,
   val apPeriodTo: LocalDate
) {

   constructor(dto: FinancialCalendarGLAPDateRangeDTO) :
      this(
         glPeriodFrom = dto.glPeriodFrom,
         glPeriodTo = dto.glPeriodTo,
         apPeriodFrom = dto.apPeriodFrom,
         apPeriodTo = dto.apPeriodTo
      )
}
