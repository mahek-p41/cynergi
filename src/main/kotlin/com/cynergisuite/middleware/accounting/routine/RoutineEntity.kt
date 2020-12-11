package com.cynergisuite.middleware.accounting.routine

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodType
import java.time.LocalDate

data class RoutineEntity(
   val id: Long? = null,
   val overallPeriod: OverallPeriodType,
   val period: Int,
   val periodFrom: LocalDate,
   val periodTo: LocalDate,
   val fiscalYear: Int,
   val generalLedgerOpen: Boolean,
   val accountPayableOpen: Boolean
) : Identifiable {

   constructor(dto: RoutineDTO, overallPeriod: OverallPeriodType) :
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

   override fun myId(): Long? = id
}
