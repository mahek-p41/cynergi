package com.cynergisuite.middleware.accounting.account.payable

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object AccountPayableInvoiceSelectedTypeDataLoader {

   @JvmStatic
   private val accountPayableInvoiceSelectedType = listOf(
      AccountPayableInvoiceSelectedType(
         id = 1,
         value = "Y",
         description = "Yes",
         localizationCode = "yes"
      ),
      AccountPayableInvoiceSelectedType(
         id = 2,
         value = "N",
         description = "No",
         localizationCode = "no"
      ),
      AccountPayableInvoiceSelectedType(
         id = 3,
         value = "H",
         description = "On Hold",
         localizationCode = "on.hold"
      )
   )

   @JvmStatic
   fun random(): AccountPayableInvoiceSelectedType {
      return accountPayableInvoiceSelectedType.random()
   }

   @JvmStatic
   fun predefined(): List<AccountPayableInvoiceSelectedType> {
      return accountPayableInvoiceSelectedType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AccountPayableInvoiceSelectedTypeDataLoaderService() {
   fun random() = AccountPayableInvoiceSelectedTypeDataLoader.random()
   fun predefined() = AccountPayableInvoiceSelectedTypeDataLoader.predefined()
}
