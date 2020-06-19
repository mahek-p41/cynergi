package com.cynergisuite.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StringExtensionsTests {

   @Test
   fun `not null blank string trim to null`() {
      val str = "   "

      assertThat(str.trimToNull()).isNull()
   }

   @Test
   fun `null string trim to null`() {
      val str: String? = null

      assertThat(str.trimToNull()).isNull()
   }

   @Test
   fun `string with characters wrapped in a space`() {
      val str = " test string "

      assertThat(str.trimToNull()).isEqualTo("test string")
   }

   @Test
   fun `string with numbers only`() {
      val str = "123"

      assertThat(str.isDigits()).isTrue()
   }

   @Test
   fun `string with some letters and digits`() {
      val str = "123ABC"

      assertThat(str.isDigits()).isFalse()
   }

   @Test
   fun `null string for digits`() {
      val str: String? = null

      assertThat(str.isDigits()).isFalse()
   }

   @Test
   fun `all uppercase`() {
      val str = "UPPERCASE"

      assertThat(str.isAllUpperCase()).isTrue()
   }

   @Test
   fun `all lowercase`() {
      val str = "lowercase"

      assertThat(str.isAllLowerCase()).isTrue()
      assertThat(str.isAllSameCase()).isTrue()
   }

   @Test
   fun `mixed case with uppercase check`() {
      val str = "mixedCase"

      assertThat(str.isAllUpperCase()).isFalse()
      assertThat(str.isAllSameCase()).isFalse()
   }

   @Test
   fun `mixed case with lowercase check`() {
      val str = "mixedCase"

      assertThat(str.isAllLowerCase()).isFalse()
      assertThat(str.isAllSameCase()).isFalse()
   }

   @Test
   fun `truncate string that is not null`() {
      val str = "the quick brown fox"

      assertThat(str.truncate(5)).isEqualTo("the q")
   }

   @Test
   fun `truncate string with width longer that provided string`() {
      val str = "short"

      assertThat(str.truncate(10)).isEqualTo("short")
   }

   @Test
   fun `truncate string that is null`() {
      val str: String? = null

      assertThat(str.truncate(10)).isNull()
   }
}
