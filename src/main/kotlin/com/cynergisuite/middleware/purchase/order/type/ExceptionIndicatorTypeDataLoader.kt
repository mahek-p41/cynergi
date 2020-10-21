package com.cynergisuite.middleware.purchase.order.type

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object ExceptionIndicatorTypeDataLoader {

   @JvmStatic
   private val ExceptionIndicatorType = listOf(
      ExceptionIndicatorType(
         id = 1,
         value = "E",
         description = "Exception",
         localizationCode = "exception"
      ),
      ExceptionIndicatorType(
         id = 2,
         value = "N",
         description = "Normal",
         localizationCode = "normal"
      ),
      ExceptionIndicatorType(
         id = 3,
         value = "P",
         description = "Promo",
         localizationCode = "promo"
      ),
      ExceptionIndicatorType(
         id = 4,
         value = "S",
         description = "Special",
         localizationCode = "special"
      )
   )

   @JvmStatic
   fun random(): ExceptionIndicatorType {
      return ExceptionIndicatorType.random()
   }

   @JvmStatic
   fun predefined(): List<ExceptionIndicatorType> {
      return ExceptionIndicatorType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class ExceptionIndicatorTypeDataLoaderService() {
   fun random() = ExceptionIndicatorTypeDataLoader.random()
   fun predefined() = ExceptionIndicatorTypeDataLoader.predefined()
}
