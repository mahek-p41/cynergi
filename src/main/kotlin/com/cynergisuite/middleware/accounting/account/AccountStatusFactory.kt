package com.cynergisuite.middleware.accounting.account

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object AccountStatusFactory {

   @JvmStatic
   private val accountStatus = listOf(
      AccountStatusType(
         id = 1,
         value = "A",
         description = "Active",
         localizationCode = "active"
      ),
      AccountStatusType(
         id = 2,
         value = "I",
         description = "Inactive",
         localizationCode = "inactive"
      )
   )

   @JvmStatic
   fun random(): AccountStatusType {
      return accountStatus.random()
   }

   @JvmStatic
   fun predefined(): List<AccountStatusType> {
      return accountStatus
   }

}

@Singleton
@Requires(env = ["develop", "test"])
class AccountStatusFactoryService(
) {
   fun random() = AccountStatusFactory.random()
   fun predefined() = AccountStatusFactory.predefined()
}
