package com.cynergisuite.middleware.shipping.location

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

class ShipLocationTypeTestDataLoader {

   private static final List<ShipLocationType> shipLocationType = [
      new ShipLocationType(
         1,
         "C",
         "Customer",
         "customer"
      ),
      new ShipLocationType(
         2,
         "S",
         "Store",
         "store"
      )
   ]

   static ShipLocationType random() { shipLocationType.random() }

   static List<ShipLocationType> predefined() { shipLocationType }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class ShipLocationTypeTestDataLoaderService {
   def random() { ShipLocationTypeTestDataLoader.random() }
   def predefined() { ShipLocationTypeTestDataLoader.predefined() }
}
