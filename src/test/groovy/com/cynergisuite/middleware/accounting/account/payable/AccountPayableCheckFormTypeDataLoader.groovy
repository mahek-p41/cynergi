package com.cynergisuite.middleware.accounting.account.payable

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

class AccountPayableCheckFormTypeDataLoader {

   private static final List<AccountPayableCheckFormType> accountPayableCheckFormType = [
      new AccountPayableCheckFormType(
         1,
         "2",
         "Laser 2",
         "laser.two"
      ),
      new AccountPayableCheckFormType(
         2,
         "3",
         "Laser 3",
         "laser.three"
      ),
      new AccountPayableCheckFormType(
         3,
         "L",
         "Laser",
         "laser"
      )
   ]

   static AccountPayableCheckFormType random() {
      return accountPayableCheckFormType.random()
   }

   static List<AccountPayableCheckFormType> predefined() {
      return accountPayableCheckFormType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayableCheckFormTypeDataLoaderService {
   def random() { AccountPayableCheckFormTypeDataLoader.random() }
   def predefined() { AccountPayableCheckFormTypeDataLoader.predefined() }
}
