package com.cynergisuite.middleware.purchase.order.type

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import jakarta.inject.Singleton

class PurchaseOrderRequisitionIndicatorTypeTestDataLoader {

   private static final List<PurchaseOrderRequisitionIndicatorType> purchaseOrderRequisitionIndicatorType = [
      new PurchaseOrderRequisitionIndicatorType(
         1,
         "P",
         "Purchase Order",
         "purchase.order"
      ),
      new PurchaseOrderRequisitionIndicatorType(
         2,
         "R",
         "Requisition",
         "requisition"
      ),
      new PurchaseOrderRequisitionIndicatorType(
         3,
         "D",
         "Deleted",
         "deleted"
      )
   ]

   static PurchaseOrderRequisitionIndicatorType random() {
      return purchaseOrderRequisitionIndicatorType.random()
   }

   static List<PurchaseOrderRequisitionIndicatorType> predefined() {
      return purchaseOrderRequisitionIndicatorType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class PurchaseOrderRequisitionIndicatorTypeTestDataLoaderService {
   def random() { PurchaseOrderRequisitionIndicatorTypeTestDataLoader.random() }
   def predefined() { PurchaseOrderRequisitionIndicatorTypeTestDataLoader.predefined() }
}
