package com.cynergisuite.middleware.accounting.account.payable

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object PrintCurrencyIndicatorTypeDataLoader {

   @JvmStatic
   private val printCurrencyIndicatorType = listOf(
      PrintCurrencyIndicatorType(
         id = 1,
         value = "B",
         description = "Bank",
         localizationCode = "bank"
      ),
      PrintCurrencyIndicatorType(
         id = 2,
         value = "N",
         description = "No",
         localizationCode = "no"
      ),
      PrintCurrencyIndicatorType(
         id = 3,
         value = "V",
         description = "Vendor",
         localizationCode = "vendor"
      )
   )

   @JvmStatic
   fun random(): PrintCurrencyIndicatorType {
      return printCurrencyIndicatorType.random()
   }

   @JvmStatic
   fun predefined(): List<PrintCurrencyIndicatorType> {
      return printCurrencyIndicatorType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class PrintCurrencyIndicatorTypeDataLoaderService() {
   fun random() = PrintCurrencyIndicatorTypeDataLoader.random()
   fun predefined() = PrintCurrencyIndicatorTypeDataLoader.predefined()
}
