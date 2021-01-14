package com.cynergisuite.middleware.purchase.order.type

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object ApprovalRequiredFlagTypeFactory {

   @JvmStatic
   private val approvalRequiredFlagType = listOf(
      ApprovalRequiredFlagType(
         id = 1,
         value = "B",
         description = "Both Purchase Order and Requisition",
         localizationCode = "both.purchase.order.and.requisition"
      ),
      ApprovalRequiredFlagType(
         id = 2,
         value = "N",
         description = "No Approval",
         localizationCode = "no.approval"
      ),
      ApprovalRequiredFlagType(
         id = 3,
         value = "P",
         description = "Purchase Order Only",
         localizationCode = "purchase.order.only"
      ),
      ApprovalRequiredFlagType(
         id = 4,
         value = "R",
         description = "Requisition Only",
         localizationCode = "requisition.only"
      )
   )

   @JvmStatic
   fun random(): ApprovalRequiredFlagType {
      return approvalRequiredFlagType.random()
   }

   @JvmStatic
   fun predefined(): List<ApprovalRequiredFlagType> {
      return approvalRequiredFlagType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class ApprovalRequiredFlagTypeFactoryService() {
   fun random() = ApprovalRequiredFlagTypeFactory.random()
   fun predefined() = ApprovalRequiredFlagTypeFactory.predefined()
}
