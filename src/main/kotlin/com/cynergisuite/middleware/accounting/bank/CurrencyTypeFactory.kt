package com.cynergisuite.middleware.accounting.bank

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object CurrencyFactory {

   @JvmStatic
   private val currencies = listOf(
      BankCurrencyType(
         id = 1,
         value = "USA",
         description = "United States",
         localizationCode = "united.states"
      ),
      BankCurrencyType(
         id = 2,
         value = "CAN",
         description = "Canada",
         localizationCode = "Canada"
      )
   )

   @JvmStatic
   fun random(): BankCurrencyType {
      return currencies.random()
   }

   @JvmStatic
   fun usd(): BankCurrencyType {
      return currencies[0]
   }

   @JvmStatic
   fun cad(): BankCurrencyType {
      return currencies[1]
   }

}

@Singleton
@Requires(env = ["develop", "test"])
class CurrencyFactoryService(
) {
   fun random(): BankCurrencyType = CurrencyFactory.random()
   fun usd(): BankCurrencyType = CurrencyFactory.usd()
   fun cad(): BankCurrencyType = CurrencyFactory.cad()
}
