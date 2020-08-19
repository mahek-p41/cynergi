package com.cynergisuite.middleware.purchase.order

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object PurchaseOrderTypeFactory {

   @JvmStatic
   private val purchaseOrderType = listOf(
      PurchaseOrderType(
         id = 1,
         value = "P",
         description = "Purchase Order",
         localizationCode = "purchase.order"
      ),
      PurchaseOrderType(
         id = 2,
         value = "R",
         description = "Requisition",
         localizationCode = "prequisition"
      ),
      PurchaseOrderType(
         id = 3,
         value = "D",
         description = "Deletes",
         localizationCode = "deletes"
      )
   )

   @JvmStatic
   fun random(): PurchaseOrderType {
      return purchaseOrderType.random()
   }

   @JvmStatic
   fun predefined(): List<PurchaseOrderType> {
      return purchaseOrderType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class PurchaseOrderTypeFactoryService() {
   fun random() = PurchaseOrderTypeFactory.random()
   fun predefined() = PurchaseOrderTypeFactory.predefined()
}
