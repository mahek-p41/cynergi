package com.cynergisuite.middleware.accounting.financial.calendar

import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "Fiscal year", title = "Fiscal Year DTO", description = "Fiscal year DTO.")
data class FiscalYearDTO(

   @field:Valid
   @field:NotNull
   @field:Schema(description = "Overall period type in the financial calendar.")
   var overallPeriod: OverallPeriodTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "Begin date")
   var begin: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "End date")
   var end: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Fiscal year")
   var fiscalYear: Int? = null,

)
