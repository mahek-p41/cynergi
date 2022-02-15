package com.cynergisuite.middleware.purchase.order.type

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import jakarta.inject.Singleton

class DefaultPurchaseOrderTypeTestDataLoader {

   private static final List<DefaultPurchaseOrderType> defaultPurchaseOrderType = [
      new DefaultPurchaseOrderType(
         1,
         "P",
         "Purchase Order",
         "purchase.order"
      ),
      new DefaultPurchaseOrderType(
         2,
         "R",
         "Requisition",
         "requisition"
      )
   ]

   static DefaultPurchaseOrderType random() {
      return defaultPurchaseOrderType.random()
   }

   static List<DefaultPurchaseOrderType> predefined() {
      return defaultPurchaseOrderType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class DefaultPurchaseOrderTypeTestDataLoaderService {
   def random() { DefaultPurchaseOrderTypeTestDataLoader.random() }
   def predefined() { DefaultPurchaseOrderTypeTestDataLoader.predefined() }
}
