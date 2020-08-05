package com.cynergisuite.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class OffsetDateTimeExtensionsTests {

   @Test
   fun `check sunday is actually a sunday`() {
      val sunday = OffsetDateTime.parse("2019-09-29T00:00:00+00:00")
      val wednesday = OffsetDateTime.parse("2019-10-02T12:35:09+00:00")

      assertThat(wednesday.beginningOfWeek()).isEqualTo(sunday)
   }

   @Test
   fun `check sunday is still sunday`() {
      val sunday = OffsetDateTime.parse("2019-09-29T00:00:00+00:00")

      assertThat(sunday.beginningOfWeek()).isEqualTo(sunday)
   }

   @Test
   fun `check saturday is still saturday`() {
      val saturday = OffsetDateTime.parse("2019-10-05T23:59:59.999999999+00:00")

      assertThat(saturday.endOfWeek()).isEqualTo(saturday)
   }

   @Test
   fun `check saturday is actually saturday`() {
      val perfectSaturday = OffsetDateTime.parse("2019-10-05T23:59:59.999999999+00:00")
      val wednesday = OffsetDateTime.parse("2019-10-02T12:35:09+00:00")

      assertThat(wednesday.endOfWeek()).isEqualTo(perfectSaturday)
   }

   @Test
   fun `check saturday after sunday`() {
      val perfectSunday = OffsetDateTime.parse("2019-10-06T00:00:00Z")
      val perfectSaturday = OffsetDateTime.parse("2019-10-12T23:59:59.999999999Z")
      val monday = OffsetDateTime.parse("2019-10-07T14:17:05.520Z")
      val sunday = monday.beginningOfWeek()
      val saturday = sunday.endOfWeek()

      assertThat(sunday).isEqualTo(perfectSunday)
      assertThat(saturday).isEqualTo(perfectSaturday)
   }
}
