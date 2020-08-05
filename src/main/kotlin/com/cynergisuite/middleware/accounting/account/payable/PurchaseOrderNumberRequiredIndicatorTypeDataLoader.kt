package com.cynergisuite.middleware.accounting.account.payable

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object PurchaseOrderNumberRequiredIndicatorTypeDataLoader {

   @JvmStatic
   private val purchaseOrderNumberRequiredIndicatorType = listOf(
      PurchaseOrderNumberRequiredIndicatorType(
         id = 1,
         value = "M",
         description = "Sometimes Validate",
         localizationCode = "sometimes.validate"
      ),
      PurchaseOrderNumberRequiredIndicatorType(
         id = 2,
         value = "N",
         description = "Never Validate",
         localizationCode = "never.validate"
      ),
      PurchaseOrderNumberRequiredIndicatorType(
         id = 3,
         value = "V",
         description = "Validate",
         localizationCode = "validate"
      )
   )

   @JvmStatic
   fun random(): PurchaseOrderNumberRequiredIndicatorType {
      return purchaseOrderNumberRequiredIndicatorType.random()
   }

   @JvmStatic
   fun predefined(): List<PurchaseOrderNumberRequiredIndicatorType> {
      return purchaseOrderNumberRequiredIndicatorType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class PurchaseOrderNumberRequiredIndicatorTypeDataLoaderService() {
   fun random() = PurchaseOrderNumberRequiredIndicatorTypeDataLoader.random()
   fun predefined() = PurchaseOrderNumberRequiredIndicatorTypeDataLoader.predefined()
}
