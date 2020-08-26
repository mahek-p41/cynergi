package com.cynergisuite.middleware.accounting.account.payable

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object AccountPayableCheckFormTypeDataLoader {

   @JvmStatic
   private val accountPayableCheckFormType = listOf(
      AccountPayableCheckFormType(
         id = 1,
         value = "2",
         description = "Laser 2",
         localizationCode = "laser.two"
      ),
      AccountPayableCheckFormType(
         id = 2,
         value = "3",
         description = "Laser 3",
         localizationCode = "laser.three"
      ),
      AccountPayableCheckFormType(
         id = 3,
         value = "L",
         description = "Laser",
         localizationCode = "laser"
      )
   )

   @JvmStatic
   fun random(): AccountPayableCheckFormType {
      return accountPayableCheckFormType.random()
   }

   @JvmStatic
   fun predefined(): List<AccountPayableCheckFormType> {
      return accountPayableCheckFormType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AccountPayableCheckFormTypeDataLoaderService() {
   fun random() = AccountPayableCheckFormTypeDataLoader.random()
   fun predefined() = AccountPayableCheckFormTypeDataLoader.predefined()
}
