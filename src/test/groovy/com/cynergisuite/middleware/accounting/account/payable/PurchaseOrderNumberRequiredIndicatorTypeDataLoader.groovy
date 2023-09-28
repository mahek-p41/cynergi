package com.cynergisuite.middleware.accounting.account.payable

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

class PurchaseOrderNumberRequiredIndicatorTypeDataLoader {

   private static final List<PurchaseOrderNumberRequiredIndicatorType> purchaseOrderNumberRequiredIndicatorType = [
      new PurchaseOrderNumberRequiredIndicatorType(
         1,
         "M",
         "Sometimes Validate",
         "sometimes.validate"
      ),
      new PurchaseOrderNumberRequiredIndicatorType(
         2,
         "N",
         "Never Validate",
         "never.validate"
      ),
      new PurchaseOrderNumberRequiredIndicatorType(
         3,
         "V",
         "Validate",
         "validate"
      )
   ]

   static PurchaseOrderNumberRequiredIndicatorType random() {
      return purchaseOrderNumberRequiredIndicatorType.random()
   }

   static List<PurchaseOrderNumberRequiredIndicatorType> predefined() {
      return purchaseOrderNumberRequiredIndicatorType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class PurchaseOrderNumberRequiredIndicatorTypeDataLoaderService {
   def random() { PurchaseOrderNumberRequiredIndicatorTypeDataLoader.random() }
   def predefined() { PurchaseOrderNumberRequiredIndicatorTypeDataLoader.predefined() }
}
