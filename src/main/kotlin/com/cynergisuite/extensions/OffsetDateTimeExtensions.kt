package com.cynergisuite.extensions

import java.time.OffsetDateTime

fun OffsetDateTime.sunday(): OffsetDateTime {
   val weekday = this.dayOfWeek.value.toLong()

   return this.minusDays(weekday).minusNanos(this.nano.toLong())
}

fun OffsetDateTime.saturday(): OffsetDateTime =
   this.sunday()
      .plusDays(7)
      .minusNanos(1)
