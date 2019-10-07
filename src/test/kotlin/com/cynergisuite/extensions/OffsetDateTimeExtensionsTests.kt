package com.cynergisuite.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class OffsetDateTimeExtensionsTests {

   @Test
   fun `check sunday is actually a sunday` () {
      val perfectSunday = OffsetDateTime.parse("2019-09-29T00:00:00+00:00")
      val wednesday = OffsetDateTime.parse("2019-10-02T12:35:09+00:00")

      assertThat(wednesday.sunday()).isEqualTo(perfectSunday)
   }

   @Test
   fun `check saturday is actually saturday` () {
      val perfectSaturday = OffsetDateTime.parse("2019-10-05T23:59:59.999999999+00:00")
      val wednesday = OffsetDateTime.parse("2019-10-02T12:35:09+00:00")

      assertThat(wednesday.saturday()).isEqualTo(perfectSaturday)
   }
}
