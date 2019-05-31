package com.cynergisuite.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StringExtensionsTests {

   @Test
   fun `not null blank string trim to null` () {
      val str = "   "

      assertThat(str.trimToNull())
         .isNull()
   }

   @Test
   fun `null string trim to null` () {
      val str: String? = null

      assertThat(str.trimToNull())
         .isNull()
   }

   @Test
   fun `string with characters wrapped in a space` () {
      val str = " test string "

      assertThat(str.trimToNull())
         .isEqualTo("test string")
   }
}
