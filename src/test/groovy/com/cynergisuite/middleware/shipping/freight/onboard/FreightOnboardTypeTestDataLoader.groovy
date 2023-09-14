package com.cynergisuite.middleware.shipping.freight.onboard

class FreightOnboardTypeTestDataLoader {
   private static final List<FreightOnboardType> freightOnboards = [
      new FreightOnboardType(
         1,
         "D",
         "Destination",
         "destination"
      ),
      new FreightOnboardType(
         2,
         "S",
         "Shipping",
         "shipping"
      )
   ]

   static FreightOnboardType random() { freightOnboards.random() }
}
