package com.cynergisuite.middleware.accounting.account.payable.payment

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object AccountPayablePaymentStatusTypeDataLoader {

   @JvmStatic
   private val accountPayablePaymentStatusType = listOf(
      AccountPayablePaymentStatusType(
         id = 1,
         value = "P",
         description = "Paid",
         localizationCode = "paid"
      ),
      AccountPayablePaymentStatusType(
         id = 2,
         value = "V",
         description = "Void",
         localizationCode = "void"
      )
   )

   @JvmStatic
   fun random(): AccountPayablePaymentStatusType {
      return accountPayablePaymentStatusType.random()
   }

   @JvmStatic
   fun predefined(): List<AccountPayablePaymentStatusType> {
      return accountPayablePaymentStatusType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AccountPayablePaymentStatusTypeDataLoaderService() {
   fun random() = AccountPayablePaymentStatusTypeDataLoader.random()
   fun predefined() = AccountPayablePaymentStatusTypeDataLoader.predefined()
}
