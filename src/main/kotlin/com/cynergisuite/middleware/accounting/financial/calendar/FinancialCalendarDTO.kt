package com.cynergisuite.middleware.accounting.financial.calendar

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "Financial calendar", title = "An entity used to build a financial calendar", description = "An entity used to build a financial calendar.")
data class FinancialCalendarDTO(

   var id: UUID? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(description = "Overall period type in the financial calendar.")
   var overallPeriod: OverallPeriodTypeDTO? = null,

   @field:NotNull
   @field:Min(value = 1)
   @field:Max(value = 12)
   @field:Schema(description = "Period", minimum = "1", maximum = "12")
   var period: Int? = null,

   @field:NotNull
   @field:Schema(description = "Period from date")
   var periodFrom: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Period to date")
   var periodTo: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Fiscal year")
   var fiscalYear: Int? = null,

   @field:NotNull
   @field:Schema(description = "General ledger open")
   var generalLedgerOpen: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Account payable open")
   var accountPayableOpen: Boolean? = null

) : Identifiable {
   constructor(entity: FinancialCalendarEntity) :
      this(
         id = entity.id,
         overallPeriod = OverallPeriodTypeDTO(entity.overallPeriod),
         period = entity.period,
         periodFrom = entity.periodFrom,
         periodTo = entity.periodTo,
         fiscalYear = entity.fiscalYear,
         generalLedgerOpen = entity.generalLedgerOpen,
         accountPayableOpen = entity.accountPayableOpen
      )

   override fun myId(): UUID? = id
}
