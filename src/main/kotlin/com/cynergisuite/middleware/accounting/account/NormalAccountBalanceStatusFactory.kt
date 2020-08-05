package com.cynergisuite.middleware.accounting.account

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object NormalAccountBalanceFactory {

   @JvmStatic
   private val normalAccountBalances = listOf(
      NormalAccountBalanceType(
         id = 1,
         value = "C",
         description = "Credit",
         localizationCode = "credit"
      ),
      NormalAccountBalanceType(
         id = 2,
         value = "D",
         description = "Debit",
         localizationCode = "debit"
      )
   )

   @JvmStatic
   fun random(): NormalAccountBalanceType {
      return normalAccountBalances.random()
   }

   @JvmStatic
   fun predefined(): List<NormalAccountBalanceType> {
      return normalAccountBalances
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class NormalAccountBalanceFactoryService() {
   fun random() = NormalAccountBalanceFactory.random()
   fun predefined() = NormalAccountBalanceFactory.predefined()
}
