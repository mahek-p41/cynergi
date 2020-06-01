package com.cynergisuite.middleware.shipping.freight.onboard

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object FreightOnboardTypeTestDataLoader {
   private val freightOnboards = listOf(
      FreightOnboardType(
         id = 1,
         value = "D",
         description = "Destination",
         localizationCode = "destination"
      ),
      FreightOnboardType(
         id = 2,
         value = "S",
         description = "Shipping",
         localizationCode = "shipping"
      )
   )

   @JvmStatic
   fun random() = freightOnboards.random()
}
