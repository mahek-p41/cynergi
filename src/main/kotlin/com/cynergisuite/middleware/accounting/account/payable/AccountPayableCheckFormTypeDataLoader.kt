package com.cynergisuite.middleware.accounting.account.payable

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object AccountPayableCheckFormTypeDataLoader {

   @JvmStatic
   private val accountPayableCheckFormType = listOf(
      AccountPayableCheckFormType(
         id = 1,
         value = "L2",
         description = "Laser 2",
         localizationCode = "laser.2"
      ),
      AccountPayableCheckFormType(
         id = 2,
         value = "L3",
         description = "Laser 3",
         localizationCode = "laser.3"
      ),
      AccountPayableCheckFormType(
         id = 3,
         value = "T",
         description = "Tractor",
         localizationCode = "tractor"
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
