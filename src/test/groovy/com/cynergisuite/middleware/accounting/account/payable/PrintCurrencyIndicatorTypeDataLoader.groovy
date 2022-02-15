package com.cynergisuite.middleware.accounting.account.payable

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

class PrintCurrencyIndicatorTypeDataLoader {

   private static final List<PrintCurrencyIndicatorType> printCurrencyIndicatorType = [
      new PrintCurrencyIndicatorType(
         1,
         "B",
         "Bank",
         "bank"
      ),
      new PrintCurrencyIndicatorType(
         2,
         "N",
         "No",
         "no"
      ),
      new PrintCurrencyIndicatorType(
         3,
         "V",
         "Vendor",
         "vendor"
      )
   ]

   static PrintCurrencyIndicatorType random() {
      return printCurrencyIndicatorType.random()
   }

   static List<PrintCurrencyIndicatorType> predefined() {
      return printCurrencyIndicatorType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class PrintCurrencyIndicatorTypeDataLoaderService {
   def random() { PrintCurrencyIndicatorTypeDataLoader.random() }
   def predefined() { PrintCurrencyIndicatorTypeDataLoader.predefined() }
}
