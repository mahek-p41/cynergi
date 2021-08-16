package com.cynergisuite.middleware.purchase.order.type

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

class UpdatePurchaseOrderCostTypeTestDataLoader {

   private static final List<UpdatePurchaseOrderCostType> updatePurchaseOrderCostType = [
      new UpdatePurchaseOrderCostType(
         1,
         "B",
         "Both Purchase Order and Requisition",
         "both.purchase.order.and.requisition"
      ),
      new UpdatePurchaseOrderCostType(
         2,
         "N",
         "No Update",
         "no.update"
      ),
      new UpdatePurchaseOrderCostType(
         3,
         "P",
         "Purchase Order Only",
         "purchase.order.only"
      ),
      new UpdatePurchaseOrderCostType(
         4,
         "R",
         "Requisition Only",
         "requisition.only"
      )
   ]

   static UpdatePurchaseOrderCostType random() {
      return updatePurchaseOrderCostType.random()
   }

   static List<UpdatePurchaseOrderCostType> predefined() {
      return updatePurchaseOrderCostType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class UpdatePurchaseOrderCostTypeTestDataLoaderService {
   def random() { UpdatePurchaseOrderCostTypeTestDataLoader.random() }
   def predefined() { UpdatePurchaseOrderCostTypeTestDataLoader.predefined() }
}
