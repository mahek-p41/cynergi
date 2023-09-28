package com.cynergisuite.middleware.accounting.financial.calendar

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "FinancialCalendarGLAPDateRange", title = "Date range(s) for setting GL and/or AP Windows")
data class FinancialCalendarGLAPDateRangeDTO(

   @field:NotNull
   @field:Schema(description = "GL period from date")
   var glPeriodFrom: LocalDate,

   @field:NotNull
   @field:Schema(description = "GL period to date")
   var glPeriodTo: LocalDate,

   @field:NotNull
   @field:Schema(description = "AP period from date")
   var apPeriodFrom: LocalDate,

   @field:NotNull
   @field:Schema(description = "AP period to date")
   var apPeriodTo: LocalDate

) {
   constructor(entity: FinancialCalendarGLAPDateRangeEntity) :
      this(
         glPeriodFrom = entity.glPeriodFrom,
         glPeriodTo = entity.glPeriodTo,
         apPeriodFrom = entity.apPeriodFrom,
         apPeriodTo = entity.apPeriodTo
      )
}
