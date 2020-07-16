package com.cynergisuite.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode.DOWN
import java.math.RoundingMode.HALF_EVEN
import java.math.RoundingMode.HALF_UP
import java.math.RoundingMode.UP

class BigDecimalExtensionsTests {

   @Test
   fun `check 7 digits with default rounding` () {
      val number = BigDecimal("1.12345678")

      assertThat(number.toFixed(7)).isEqualTo("1.1234568")
   }

   @Test
   fun `check 7 digits with round down` () {
      val number = BigDecimal("1.12345678")

      assertThat(number.toFixed(7, roundingMode = DOWN)).isEqualTo("1.1234567")
   }

   @Test
   fun `check 7 digits with round up` () {
      val number = BigDecimal("1.12345678")

      assertThat(number.toFixed(7, roundingMode = UP)).isEqualTo("1.1234568")
   }

   @Test
   fun `check with minimal fraction digits` () {
      val number = BigDecimal("1.1")

      assertThat(number.toFixed(7, 2)).isEqualTo("1.10")
   }

   @Test
   fun `check equalTo without scale is equal` () {
      val left = BigDecimal("2.00").setScale(3, HALF_EVEN)
      val right = BigDecimal("2").setScale(2, HALF_UP)

      assertThat(left.equalTo(right)).isTrue()
   }

   @Test
   fun `check equalTo without scale is equal but numbers are different` () {
      val left = BigDecimal("3.00").setScale(3, HALF_EVEN)
      val right = BigDecimal("2.00").setScale(2, HALF_EVEN)

      assertThat(left.equalTo(right)).isFalse()
   }
}
