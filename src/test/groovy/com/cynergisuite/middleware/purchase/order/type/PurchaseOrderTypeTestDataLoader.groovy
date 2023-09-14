package com.cynergisuite.middleware.purchase.order.type

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

class PurchaseOrderTypeTestDataLoader {

   private static final List<PurchaseOrderType> purchaseOrderType = [
      new PurchaseOrderType(
         1,
         "P",
         "Purchase Order",
         "purchase.order"
      ),
      new PurchaseOrderType(
         2,
         "R",
         "Requisition",
         "requisition"
      ),
      new PurchaseOrderType(
         3,
         "D",
         "Deletes",
         "deletes"
      )
   ]

   static PurchaseOrderType random() {
      return purchaseOrderType.random()
   }

   static List<PurchaseOrderType> predefined() {
      return purchaseOrderType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class PurchaseOrderTypeTestDataLoaderService {
   def random() { PurchaseOrderTypeTestDataLoader.random() }
   def predefined() { PurchaseOrderTypeTestDataLoader.predefined() }
}
