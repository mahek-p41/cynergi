package com.cynergisuite.middleware.accounting.account.payable.payment

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

class AccountPayablePaymentTypeTypeDataLoader {

   private static final List<AccountPayablePaymentTypeType> accountPayablePaymentTypeType = [
      new AccountPayablePaymentTypeType(
         1,
         "A",
         "ACH",
         "ach"
      ),
      new AccountPayablePaymentTypeType(
         2,
         "C",
         "Check",
         "check"
      )
   ]

   static AccountPayablePaymentTypeType random() {
      return accountPayablePaymentTypeType.random()
   }

   static List<AccountPayablePaymentTypeType> predefined() {
      return accountPayablePaymentTypeType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayablePaymentTypeTypeDataLoaderService {
   def random() { AccountPayablePaymentTypeTypeDataLoader.random() }
   def predefined() { AccountPayablePaymentTypeTypeDataLoader.predefined() }
}
