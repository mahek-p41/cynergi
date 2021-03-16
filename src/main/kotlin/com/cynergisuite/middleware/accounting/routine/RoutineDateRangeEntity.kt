package com.cynergisuite.middleware.accounting.routine

import java.time.LocalDate

data class RoutineDateRangeEntity(
   val periodFrom: LocalDate,
   val periodTo: LocalDate
) {

   constructor(dto: RoutineDateRangeDTO):
      this(
         periodFrom = dto.periodFrom!!,
         periodTo = dto.periodTo!!
      )
}
