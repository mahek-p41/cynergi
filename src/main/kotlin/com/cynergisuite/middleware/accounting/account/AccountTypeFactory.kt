package com.cynergisuite.middleware.accounting.account

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object AccountTypeFactory {

   @JvmStatic
   private val accountType = listOf(
      AccountType(
         id = 1,
         value = "A",
         description = "Asset Account",
         localizationCode = "asset.account"
      ),
      AccountType(
         id = 2,
         value = "C",
         description = "Capital Account",
         localizationCode = "capital.account"
      ),
      AccountType(
         id = 3,
         value = "E",
         description = "Expense Account",
         localizationCode = "expense.account"
      ),
      AccountType(
         id = 4,
         value = "L",
         description = "Liability Account",
         localizationCode = "liability.account"
      ),
      AccountType(
         id = 5,
         value = "R",
         description = "Revenue Account",
         localizationCode = "revenue.account"
      )
   )

   @JvmStatic
   fun random(): AccountType {
      return accountType.random()
   }

   @JvmStatic
   fun predefined(): List<AccountType> {
      return accountType
   }

}

@Singleton
@Requires(env = ["develop", "test"])
class AccountTypeFactoryService(
) {
   fun random() = AccountTypeFactory.random()
   fun predefined() = AccountTypeFactory.predefined()
}
