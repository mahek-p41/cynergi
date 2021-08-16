package com.cynergisuite.middleware.accounting.account.payable

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

class AccountPayableInvoiceSelectedTypeDataLoader {

   private static final List<AccountPayableInvoiceSelectedType> accountPayableInvoiceSelectedType = [
      new AccountPayableInvoiceSelectedType(
         1,
         "Y",
         "Yes",
         "yes"
      ),
      new AccountPayableInvoiceSelectedType(
         2,
         "N",
         "No",
         "no"
      ),
      new AccountPayableInvoiceSelectedType(
         3,
         "H",
         "On Hold",
         "on.hold"
      )
   ]

   static AccountPayableInvoiceSelectedType random() {
      return accountPayableInvoiceSelectedType.random()
   }

   static List<AccountPayableInvoiceSelectedType> predefined() {
      return accountPayableInvoiceSelectedType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayableInvoiceSelectedTypeDataLoaderService {
   def random() { AccountPayableInvoiceSelectedTypeDataLoader.random() }
   def predefined() { AccountPayableInvoiceSelectedTypeDataLoader.predefined() }
}
