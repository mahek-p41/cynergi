package com.cynergisuite.middleware.purchase.order.type

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object PurchaseOrderRequisitionIndicatorTypeDataLoader {

   @JvmStatic
   private val purchaseOrderRequisitionIndicatorType = listOf(
      PurchaseOrderRequisitionIndicatorType(
         id = 1,
         value = "P",
         description = "Purchase Order",
         localizationCode = "purchase.order"
      ),
      PurchaseOrderRequisitionIndicatorType(
         id = 2,
         value = "R",
         description = "Requisition",
         localizationCode = "requisition"
      ),
      PurchaseOrderRequisitionIndicatorType(
         id = 3,
         value = "D",
         description = "Deleted",
         localizationCode = "deleted"
      )
   )

   @JvmStatic
   fun random(): PurchaseOrderRequisitionIndicatorType {
      return purchaseOrderRequisitionIndicatorType.random()
   }

   @JvmStatic
   fun predefined(): List<PurchaseOrderRequisitionIndicatorType> {
      return purchaseOrderRequisitionIndicatorType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class PurchaseOrderRequisitionIndicatorTypeDataLoaderService() {
   fun random() = PurchaseOrderRequisitionIndicatorTypeDataLoader.random()
   fun predefined() = PurchaseOrderRequisitionIndicatorTypeDataLoader.predefined()
}
