package com.cynergisuite.middleware.purchase.order.type


import io.micronaut.context.annotation.Requires

import javax.inject.Singleton

class PurchaseOrderStatusTypeTestDataLoader {

   private static final List<PurchaseOrderStatusType> purchaseOrderStatusType = [
      new PurchaseOrderStatusType(
         1,
         "B",
         "Backorder",
         "backorder"
      ),
      new PurchaseOrderStatusType(
         2,
         "C",
         "Cancelled",
         "cancelled"
      ),
      new PurchaseOrderStatusType(
         3,
         "H",
         "Hold",
         "hold"
      ),
      new PurchaseOrderStatusType(
         4,
         "O",
         "Open",
         "open"
      ),
      new PurchaseOrderStatusType(
         5,
         "P",
         "Paid",
         "paid"
      ),
      new PurchaseOrderStatusType(
         6,
         "R",
         "Received",
         "received"
      )
   ]

   static PurchaseOrderStatusType random() {
      return purchaseOrderStatusType.random()
   }

   static List<PurchaseOrderStatusType> predefined() {
      return purchaseOrderStatusType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class PurchaseOrderStatusTypeTestDataLoaderService {
   def random() { PurchaseOrderStatusTypeTestDataLoader.random() }
   def predefined() { PurchaseOrderStatusTypeTestDataLoader.predefined() }
}
