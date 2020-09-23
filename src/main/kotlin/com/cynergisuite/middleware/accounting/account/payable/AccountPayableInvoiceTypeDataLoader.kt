package com.cynergisuite.middleware.accounting.account.payable

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object AccountPayableInvoiceTypeDataLoader {

   @JvmStatic
   private val accountPayableInvoiceType = listOf(
      AccountPayableInvoiceType(
         id = 1,
         value = "E",
         description = "Non Inventory",
         localizationCode = "non.inventory"
      ),
      AccountPayableInvoiceType(
         id = 2,
         value = "P",
         description = "Inventory",
         localizationCode = "inventory"
      )
   )

   @JvmStatic
   fun random(): AccountPayableInvoiceType {
      return accountPayableInvoiceType.random()
   }

   @JvmStatic
   fun predefined(): List<AccountPayableInvoiceType> {
      return accountPayableInvoiceType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AccountPayableInvoiceTypeDataLoaderService() {
   fun random() = AccountPayableInvoiceTypeDataLoader.random()
   fun predefined() = AccountPayableInvoiceTypeDataLoader.predefined()
}
