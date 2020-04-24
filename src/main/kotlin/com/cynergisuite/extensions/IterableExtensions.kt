package com.cynergisuite.extensions

import java.math.BigDecimal

fun Sequence<BigDecimal>.sum(): BigDecimal {
   var sum: BigDecimal = BigDecimal.ZERO
   for (element in this) {
      sum += element
   }
   return sum
}
