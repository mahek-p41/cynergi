package com.cynergisuite.middleware.purchase.order.type

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import jakarta.inject.Singleton

class ExceptionIndicatorTypeTestDataLoader {

   private static final List<ExceptionIndicatorType> exceptionIndicatorTypes = [
      new ExceptionIndicatorType(
         1,
         "E",
         "Exception",
         "exception"
      ),
      new ExceptionIndicatorType(
         2,
         "N",
         "Normal",
         "normal"
      ),
      new ExceptionIndicatorType(
         3,
         "P",
         "Promo",
         "promo"
      ),
      new ExceptionIndicatorType(
         4,
         "S",
         "Special",
         "special"
      )
   ]

   static ExceptionIndicatorType random() {
      return exceptionIndicatorTypes.random()
   }

   static List<ExceptionIndicatorType> predefined() {
      return exceptionIndicatorTypes
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class ExceptionIndicatorTypeTestDataLoaderService {
   def random() { ExceptionIndicatorTypeTestDataLoader.random() }
   def predefined() { ExceptionIndicatorTypeTestDataLoader.predefined() }
}
