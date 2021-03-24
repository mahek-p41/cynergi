package com.cynergisuite.middleware.accounting.account.payable.payment

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object AccountPayablePaymentTypeTypeDataLoader {

   @JvmStatic
   private val accountPayablePaymentTypeType = listOf(
      AccountPayablePaymentTypeType(
         id = 1,
         value = "A",
         description = "ACH",
         localizationCode = "ach"
      ),
      AccountPayablePaymentTypeType(
         id = 2,
         value = "C",
         description = "Check",
         localizationCode = "check"
      )
   )

   @JvmStatic
   fun random(): AccountPayablePaymentTypeType {
      return accountPayablePaymentTypeType.random()
   }

   @JvmStatic
   fun predefined(): List<AccountPayablePaymentTypeType> {
      return accountPayablePaymentTypeType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AccountPayablePaymentTypeTypeDataLoaderService() {
   fun random() = AccountPayablePaymentTypeTypeDataLoader.random()
   fun predefined() = AccountPayablePaymentTypeTypeDataLoader.predefined()
}
