package com.cynergisuite.extensions

import java.time.DayOfWeek
import java.time.OffsetDateTime

fun OffsetDateTime.beginningOfWeek(): OffsetDateTime {
   val weekday: Int = if (this.dayOfWeek == DayOfWeek.SUNDAY) 0 else this.dayOfWeek.value

   return this.minusDays(weekday.toLong())
      .withHour(0)
      .withMinute(0)
      .withSecond(0)
      .withNano(0)
}

fun OffsetDateTime.endOfWeek(): OffsetDateTime =
   this.beginningOfWeek()
      .plusDays(7)
      .minusNanos(1)

fun OffsetDateTime.beginningOfDay(): OffsetDateTime {
   return this
      .withHour(0)
      .withMinute(0)
      .withSecond(0)
      .withNano(0)
}

fun OffsetDateTime.endOfDay(): OffsetDateTime {
   return this
      .withHour(23)
      .withMinute(59)
      .withSecond(59)
      .withNano(999999999)
}
