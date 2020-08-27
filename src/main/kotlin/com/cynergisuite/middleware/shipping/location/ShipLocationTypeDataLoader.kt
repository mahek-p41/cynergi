package com.cynergisuite.middleware.shipping.location

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object ShipLocationTypeDataLoader {

   @JvmStatic
   private val shipLocationType = listOf(
      ShipLocationType(
         id = 1,
         value = "C",
         description = "Customer",
         localizationCode = "customer"
      ),
      ShipLocationType(
         id = 2,
         value = "S",
         description = "Store",
         localizationCode = "store"
      )
   )

   @JvmStatic
   fun random() = shipLocationType.random()

   @JvmStatic
   fun predefined(): List<ShipLocationType> = shipLocationType
}

@Singleton
@Requires(env = ["develop", "test"])
class ShipLocationTypeDataLoaderService() {
   fun random() = ShipLocationTypeDataLoader.random()
   fun predefined() = ShipLocationTypeDataLoader.predefined()
}
