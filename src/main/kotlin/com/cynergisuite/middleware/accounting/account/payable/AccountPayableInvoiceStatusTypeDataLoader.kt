package com.cynergisuite.middleware.accounting.account.payable

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object AccountPayableInvoiceStatusTypeDataLoader {

   @JvmStatic
   private val accountPayableInvoiceStatusType = listOf(
      AccountPayableInvoiceStatusType(
         id = 1,
         value = "H",
         description = "Hold",
         localizationCode = "hold"
      ),
      AccountPayableInvoiceStatusType(
         id = 2,
         value = "O",
         description = "Open",
         localizationCode = "open"
      ),
      AccountPayableInvoiceStatusType(
      id = 3,
      value = "P",
      description = "Paid",
      localizationCode = "paid"
      ),
      AccountPayableInvoiceStatusType(
      id = 4,
      value = "D",
      description = "Deleted",
      localizationCode = "deleted"
      )
   )

   @JvmStatic
   fun random(): AccountPayableInvoiceStatusType {
      return accountPayableInvoiceStatusType.random()
   }

   @JvmStatic
   fun predefined(): List<AccountPayableInvoiceStatusType> {
      return accountPayableInvoiceStatusType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AccountPayableInvoiceStatusTypeDataLoaderService() {
   fun random() = AccountPayableInvoiceStatusTypeDataLoader.random()
   fun predefined() = AccountPayableInvoiceStatusTypeDataLoader.predefined()
}
