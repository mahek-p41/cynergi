package com.cynergisuite.extensions

import java.math.BigDecimal

fun Sequence<BigDecimal>.sum(): BigDecimal {
   var sum: BigDecimal = BigDecimal.ZERO
   for (element in this) {
      sum += element
   }
   return sum
}

fun <E> Iterable<E>.sumByBigDecimal(selector: (E) -> BigDecimal): BigDecimal {
   var sum: BigDecimal = BigDecimal.ZERO
   for (element in this) {
      sum += selector(element)
   }
   return sum
}
