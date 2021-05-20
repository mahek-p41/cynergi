package com.cynergisuite.middleware.accounting.account.payable

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

class AccountPayableRecurringInvoiceStatusTypeDataLoader {

   private static final List<AccountPayableRecurringInvoiceStatusType> accountPayableRecurringInvoiceStatusType = [
      new AccountPayableRecurringInvoiceStatusType(
         1,
         "A",
         "Active",
         "active"
      ),
      new AccountPayableRecurringInvoiceStatusType(
         2,
         "I",
         "Inactive",
         "inactive"
      )
   ]

   static AccountPayableRecurringInvoiceStatusType random() {
      return accountPayableRecurringInvoiceStatusType.random()
   }

   static List<AccountPayableRecurringInvoiceStatusType> predefined() {
      return accountPayableRecurringInvoiceStatusType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayableRecurringInvoiceStatusTypeDataLoaderService {
   def random() { AccountPayableRecurringInvoiceStatusTypeDataLoader.random() }
   def predefined() { AccountPayableRecurringInvoiceStatusTypeDataLoader.predefined() }
}
