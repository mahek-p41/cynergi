package com.cynergisuite.middleware.accounting.account.payable

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

class AccountPayableInvoiceStatusTypeDataLoader {

   private static final List<AccountPayableInvoiceStatusType> accountPayableInvoiceStatusType = [
      new AccountPayableInvoiceStatusType(
         1,
         "H",
         "Hold",
         "hold"
      ),
      new AccountPayableInvoiceStatusType(
         2,
         "O",
         "Open",
         "open"
      ),
      new AccountPayableInvoiceStatusType(
         3,
         "P",
         "Paid",
         "paid"
      ),
      new AccountPayableInvoiceStatusType(
         4,
         "V",
         "Voided",
         "voided"
      )
   ]

   static AccountPayableInvoiceStatusType random() {
      return accountPayableInvoiceStatusType.random()
   }

   static List<AccountPayableInvoiceStatusType> predefined() {
      return accountPayableInvoiceStatusType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayableInvoiceStatusTypeDataLoaderService {
   def random() { AccountPayableInvoiceStatusTypeDataLoader.random() }
   def predefined() { AccountPayableInvoiceStatusTypeDataLoader.predefined() }
}
