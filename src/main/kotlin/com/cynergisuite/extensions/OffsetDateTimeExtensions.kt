package com.cynergisuite.extensions

import java.time.DayOfWeek
import java.time.OffsetDateTime

fun OffsetDateTime.beginningOfWeek(): OffsetDateTime {
   val weekday: Int = if (this.dayOfWeek == DayOfWeek.SUNDAY) 0 else this.dayOfWeek.value

   return this.minusDays(weekday.toLong())
      .minusHours(this.hour.toLong())
      .minusMinutes(this.minute.toLong())
      .minusSeconds(this.second.toLong())
      .minusNanos(this.nano.toLong())
}

fun OffsetDateTime.endOfWeek(): OffsetDateTime =
   this.beginningOfWeek()
      .plusDays(7)
      .minusNanos(1)
