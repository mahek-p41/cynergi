package com.cynergisuite.middleware.accounting.bank

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object CurrencyFactory {

   @JvmStatic
   private val currencies = listOf(
      BankCurrencyType(
         id = 1,
         value = "USA",
         description = "U.S. Dollar",
         localizationCode = "united.states.dollar"
      ),
      BankCurrencyType(
         id = 2,
         value = "CAN",
         description = "Canadian Dollar",
         localizationCode = "canadian.dollar"
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

   @JvmStatic
   fun predefined(): List<BankCurrencyType> {
      return currencies
   }

}

@Singleton
@Requires(env = ["develop", "test"])
class CurrencyFactoryService(
) {
   fun random() = CurrencyFactory.random()
   fun usd() = CurrencyFactory.usd()
   fun cad() = CurrencyFactory.cad()
   fun predefined() = CurrencyFactory.predefined()
}
