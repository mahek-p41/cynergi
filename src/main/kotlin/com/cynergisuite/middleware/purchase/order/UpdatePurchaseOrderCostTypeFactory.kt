package com.cynergisuite.middleware.purchase.order

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object UpdatePurchaseOrderCostTypeFactory {

   @JvmStatic
   private val updatePurchaseOrderCostType = listOf(
      UpdatePurchaseOrderCostType(
         id = 1,
         value = "B",
         description = "Both Purchase Order and Requisition",
         localizationCode = "both.purchase.order.and.requisition"
      ),
      UpdatePurchaseOrderCostType(
         id = 2,
         value = "N",
         description = "No Update",
         localizationCode = "no.update"
      ),
      UpdatePurchaseOrderCostType(
         id = 3,
         value = "P",
         description = "Purchase Order Only",
         localizationCode = "purchase.order.only"
      ),
      UpdatePurchaseOrderCostType(
         id = 4,
         value = "R",
         description = "Requisition Only",
         localizationCode = "requisition.only"
      )
   )

   @JvmStatic
   fun random(): UpdatePurchaseOrderCostType {
      return updatePurchaseOrderCostType.random()
   }

   @JvmStatic
   fun predefined(): List<UpdatePurchaseOrderCostType> {
      return updatePurchaseOrderCostType
   }

}

@Singleton
@Requires(env = ["develop", "test"])
class UpdatePurchaseOrderCostTypeFactoryService(
) {
   fun random() = UpdatePurchaseOrderCostTypeFactory.random()
   fun predefined() = UpdatePurchaseOrderCostTypeFactory.predefined()
}
