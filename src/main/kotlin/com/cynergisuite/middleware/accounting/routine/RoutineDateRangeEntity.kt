package com.cynergisuite.middleware.accounting.routine

import java.time.LocalDate

data class RoutineDateRangeEntity(
   val periodFrom: LocalDate,
   val periodTo: LocalDate
){
}
