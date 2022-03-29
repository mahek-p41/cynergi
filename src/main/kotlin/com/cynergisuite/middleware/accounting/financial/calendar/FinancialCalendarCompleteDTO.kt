package com.cynergisuite.middleware.accounting.financial.calendar

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull

@Introspected
@Schema(name = "Financial calendar complete ", title = "An entity used to build a complete financial calendar", description = "An entity used to build a complete financial calendar.")
class FinancialCalendarCompleteDTO {

   @field:NotNull
   @field:Schema(description = "Current Fiscal Year.")
   var year: Int? = null

   @field:NotNull
   @field:Schema(description = "Beginning period.")
   var periodFrom: LocalDate? = null

}
