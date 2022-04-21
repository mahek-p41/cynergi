package com.cynergisuite.extensions

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import java.time.DayOfWeek
import java.util.UUID

fun String?.trimToNull(): String? =
   StringUtils.trimToNull(this)

fun String?.isDigits(): Boolean =
   NumberUtils.isDigits(this)

/**
 * Check whether a String can be converted into a valid Java number (think int, long, float or double)
 */
fun String?.isNumber(): Boolean =
   NumberUtils.isCreatable(this)

fun String.isAllUpperCase(): Boolean =
   StringUtils.isAllUpperCase(this)

fun String.isAllLowerCase(): Boolean =
   StringUtils.isAllLowerCase(this)

fun String.isAllSameCase(): Boolean =
   this.isAllUpperCase() || this.isAllLowerCase()

fun String?.truncate(maxWidth: Int): String? =
   StringUtils.truncate(this, maxWidth)

fun String?.toUuid(): UUID? =
   this?.let { UUID.fromString(this) }

fun String?.toDayOfWeek(): DayOfWeek? =
   try {
      this?.let { DayOfWeek.valueOf(this) }
   } catch (e: java.lang.IllegalArgumentException) {
      null
   }
