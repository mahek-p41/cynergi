package com.cynergisuite.middleware.purchase.order.type

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

class ApprovalRequiredFlagTypeTestDataLoader {

   private static final List<ApprovalRequiredFlagType> approvalRequiredFlagType = [
      new ApprovalRequiredFlagType(
         1,
         "B",
         "Both Purchase Order and Requisition",
         "both.purchase.order.and.requisition"
      ),
      new ApprovalRequiredFlagType(
         2,
         "N",
         "No Approval",
         "no.approval"
      ),
      new ApprovalRequiredFlagType(
         3,
         "P",
         "Purchase Order Only",
         "purchase.order.only"
      ),
      new ApprovalRequiredFlagType(
         4,
         "R",
         "Requisition Only",
         "requisition.only"
      )
   ]

   static ApprovalRequiredFlagType random() {
      return approvalRequiredFlagType.random()
   }

   static List<ApprovalRequiredFlagType> predefined() {
      return approvalRequiredFlagType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class ApprovalRequiredFlagTypeTestDataLoaderService {
   def random() { ApprovalRequiredFlagTypeTestDataLoader.random() }
   def predefined() { ApprovalRequiredFlagTypeTestDataLoader.predefined() }
}
