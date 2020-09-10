package com.cynergisuite.middleware.purchase.order

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object PurchaseOrderStatusTypeFactory {

   @JvmStatic
   private val purchaseOrderStatusType = listOf(
      PurchaseOrderStatusType(
         id = 1,
         value = "B",
         description = "Backorder",
         localizationCode = "backorder"
      ),
      PurchaseOrderStatusType(
         id = 2,
         value = "C",
         description = "Cancelled",
         localizationCode = "cancelled"
      ),
      PurchaseOrderStatusType(
         id = 3,
         value = "H",
         description = "Hold",
         localizationCode = "hold"
      ),
      PurchaseOrderStatusType(
         id = 4,
         value = "O",
         description = "Open",
         localizationCode = "open"
      ),
      PurchaseOrderStatusType(
         id = 5,
         value = "P",
         description = "Paid",
         localizationCode = "paid"
      ),
      PurchaseOrderStatusType(
         id = 6,
         value = "R",
         description = "Received",
         localizationCode = "received"
      )
   )

   @JvmStatic
   fun random(): PurchaseOrderStatusType {
      return purchaseOrderStatusType.random()
   }

   @JvmStatic
   fun predefined(): List<PurchaseOrderStatusType> {
      return purchaseOrderStatusType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class PurchaseOrderStatusTypeFactoryService() {
   fun random() = PurchaseOrderStatusTypeFactory.random()
   fun predefined() = PurchaseOrderStatusTypeFactory.predefined()
}
