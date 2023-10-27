package com.cynergisuite.middleware.accounting.account.payable

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

class AccountPayableInvoiceTypeDataLoader {

   private final static List<AccountPayableInvoiceType> accountPayableInvoiceType = [
      new AccountPayableInvoiceType(
         1,
         "E",
         "Non Inventory",
         "non.inventory"
      ),
      new AccountPayableInvoiceType(
         2,
         "P",
         "Inventory",
         "inventory"
      )
   ]

   static AccountPayableInvoiceType random() {
      return accountPayableInvoiceType.random()
   }

   static List<AccountPayableInvoiceType> predefined() {
      return accountPayableInvoiceType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayableInvoiceTypeDataLoaderService {
   def random() { AccountPayableInvoiceTypeDataLoader.random() }
   def predefined() { AccountPayableInvoiceTypeDataLoader.predefined() }
}
