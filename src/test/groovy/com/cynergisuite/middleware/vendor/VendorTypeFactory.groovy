package com.cynergisuite.middleware.vendor

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton


class VendorTypeFactory {

   private static final List<VendorType> vendorType = [
      new VendorType(
         1,
         1,
         "Rents",
         "rents"
      ),
      new VendorType(
         2,
         2,
         "Royalties",
         "royalties"
      )
   ]

   static VendorType random() {
      return vendorType.random()
   }

   static List<VendorType> predefined() {
      return vendorType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class VendorTypeFactoryService {
   VendorType random() {
      VendorTypeFactory.random()
   }

   List<VendorType> predefined() {
      VendorTypeFactory.predefined()
   }
}
