package com.cynergisuite.middleware.purchase.order.type

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object DefaultPurchaseOrderTypeDataLoader {

   @JvmStatic
   private val defaultPurchaseOrderType = listOf(
      DefaultPurchaseOrderType(
         id = 1,
         value = "P",
         description = "Purchase Order",
         localizationCode = "purchase.order"
      ),
      DefaultPurchaseOrderType(
         id = 2,
         value = "R",
         description = "Requisition",
         localizationCode = "requisition"
      )
   )

   @JvmStatic
   fun random(): DefaultPurchaseOrderType {
      return defaultPurchaseOrderType.random()
   }

   @JvmStatic
   fun predefined(): List<DefaultPurchaseOrderType> {
      return defaultPurchaseOrderType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class DefaultPurchaseOrderTypeDataLoaderService() {
   fun random() = DefaultPurchaseOrderTypeDataLoader.random()
   fun predefined() = DefaultPurchaseOrderTypeDataLoader.predefined()
}
