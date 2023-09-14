package com.cynergisuite.middleware.accounting.account.payable.payment

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

class AccountPayablePaymentStatusTypeDataLoader {

   private static final List<AccountPayablePaymentStatusType> accountPayablePaymentStatusType = [
      new AccountPayablePaymentStatusType(
         1,
         "P",
         "Paid",
         "paid"
      ),
      new AccountPayablePaymentStatusType(
         2,
         "V",
         "Void",
         "void"
      )
   ]

   static AccountPayablePaymentStatusType random() {
      return accountPayablePaymentStatusType.random()
   }

   static List<AccountPayablePaymentStatusType> predefined()  {
      return accountPayablePaymentStatusType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayablePaymentStatusTypeDataLoaderService {
   def random() { AccountPayablePaymentStatusTypeDataLoader.random() }
   def predefined() { AccountPayablePaymentStatusTypeDataLoader.predefined() }
}
