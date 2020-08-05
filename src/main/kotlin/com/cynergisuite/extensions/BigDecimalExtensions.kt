package com.cynergisuite.extensions

import java.math.BigDecimal
import java.math.RoundingMode
import java.math.RoundingMode.HALF_EVEN
import java.text.DecimalFormat

fun BigDecimal.toFixed(maxFractionDigits: Int, minFractionDigits: Int = 0, roundingMode: RoundingMode = HALF_EVEN): String {
   val format = DecimalFormat()
   val copy = this.setScale(maxFractionDigits, roundingMode)

   format.maximumFractionDigits = maxFractionDigits
   format.minimumFractionDigits = minFractionDigits
   format.isGroupingUsed = false

   return format.format(copy)
}

/**
 * Provides the ability to do equality checks without having to worry about scale
 */
fun BigDecimal.equalTo(num: BigDecimal): Boolean =
   this.compareTo(num) == 0
