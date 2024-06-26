package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.middleware.accounting.account.type.AccountType
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

class AccountTypeFactory {

   private static final List<AccountType> accountType = [
      new AccountType(
         1,
         "A",
         "Asset Account",
         "asset.account"
      ),
      new AccountType(
         2,
         "C",
         "Capital Account",
         "capital.account"
      ),
      new AccountType(
         3,
         "E",
         "Expense Account",
         "expense.account"
      ),
      new AccountType(
         4,
         "L",
         "Liability Account",
         "liability.account"
      ),
      new AccountType(
         5,
         "R",
         "Revenue Account",
         "revenue.account"
      )
   ]

   static AccountType random() {
      return accountType.random()
   }

   static List<AccountType> predefined() {
      return accountType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountTypeFactoryService {

   def random() {
      return AccountTypeFactory.random()
   }

   def predefined() {
      return AccountTypeFactory.predefined()
   }
}
