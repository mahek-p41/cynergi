package com.cynergisuite.middleware.accounting.account.payable

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object AccountPayableRecurringInvoiceStatusTypeDataLoader {

   @JvmStatic
   private val accountPayableRecurringInvoiceStatusType = listOf(
      AccountPayableRecurringInvoiceStatusType(
         id = 1,
         value = "A",
         description = "Active",
         localizationCode = "active"
      ),
      AccountPayableRecurringInvoiceStatusType(
         id = 2,
         value = "I",
         description = "Inactive",
         localizationCode = "inactive"
      )
   )

   @JvmStatic
   fun random(): AccountPayableRecurringInvoiceStatusType {
      return accountPayableRecurringInvoiceStatusType.random()
   }

   @JvmStatic
   fun predefined(): List<AccountPayableRecurringInvoiceStatusType> {
      return accountPayableRecurringInvoiceStatusType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AccountPayableRecurringInvoiceStatusTypeDataLoaderService() {
   fun random() = AccountPayableRecurringInvoiceStatusTypeDataLoader.random()
   fun predefined() = AccountPayableRecurringInvoiceStatusTypeDataLoader.predefined()
}
