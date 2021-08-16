package com.cynergisuite.middleware.accounting.account

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

class AccountStatusFactory {

   private static final List<AccountStatusType> accountStatus = [
      new AccountStatusType(
         1,
         "A",
         "Active",
         "active"
      ),
      new AccountStatusType(
         2,
         "I",
         "Inactive",
         "inactive"
      )
   ]

   static AccountStatusType random() {
      return accountStatus.random()
   }

   static List<AccountStatusType> predefined() {
      return accountStatus
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountStatusFactoryService {
   AccountStatusType random() {
      AccountStatusFactory.random()
   }

   List<AccountStatusType> predefined() {
      AccountStatusFactory.predefined()
   }
}
