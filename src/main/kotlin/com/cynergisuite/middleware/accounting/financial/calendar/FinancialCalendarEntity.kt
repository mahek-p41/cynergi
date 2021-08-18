package com.cynergisuite.middleware.accounting.financial.calendar

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodType
import java.time.LocalDate
import java.util.UUID

data class FinancialCalendarEntity(
   val id: UUID? = null,
   val overallPeriod: OverallPeriodType,
   val period: Int,
   val periodFrom: LocalDate,
   val periodTo: LocalDate,
   val fiscalYear: Int,
   val generalLedgerOpen: Boolean,
   val accountPayableOpen: Boolean
) : Identifiable {

   constructor(dto: FinancialCalendarDTO, overallPeriod: OverallPeriodType) :
      this(
         id = dto.id,
         overallPeriod = overallPeriod,
         period = dto.period!!,
         periodFrom = dto.periodFrom!!,
         periodTo = dto.periodTo!!,
         fiscalYear = dto.fiscalYear!!,
         generalLedgerOpen = dto.generalLedgerOpen!!,
         accountPayableOpen = dto.accountPayableOpen!!
      )

   override fun myId(): UUID? = id
}
