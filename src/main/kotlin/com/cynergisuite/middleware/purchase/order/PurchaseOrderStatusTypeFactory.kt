package com.cynergisuite.middleware.purchase.order

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton
import kotlin.random.Random

object PurchaseOrderStatusTypeFactory {

   @JvmStatic
   private val purchaseOrderStatusType = listOf(
      PurchaseOrderStatusType(
         id = 1,
         value = "B",
         description = "Backorder",
         localizationCode = "backorder",
         possibleDefault = false
      ),
      PurchaseOrderStatusType(
         id = 2,
         value = "C",
         description = "Cancelled",
         localizationCode = "cancelled",
         possibleDefault = false
      ),
      PurchaseOrderStatusType(
         id = 3,
         value = "H",
         description = "Hold",
         localizationCode = "hold",
         possibleDefault = true
      ),
      PurchaseOrderStatusType(
         id = 4,
         value = "O",
         description = "Open",
         localizationCode = "open",
         possibleDefault = true
      ),
      PurchaseOrderStatusType(
         id = 5,
         value = "P",
         description = "Paid",
         localizationCode = "paid",
         possibleDefault = false
      ),
      PurchaseOrderStatusType(
         id = 6,
         value = "R",
         description = "Received",
         localizationCode = "received",
         possibleDefault = false
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
class PurchaseOrderStatusTypeFactoryService(
) {
   fun random() = PurchaseOrderStatusTypeFactory.random()
   fun predefined() = PurchaseOrderStatusTypeFactory.predefined()
}
