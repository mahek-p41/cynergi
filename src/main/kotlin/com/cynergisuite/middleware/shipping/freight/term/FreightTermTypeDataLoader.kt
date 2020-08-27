package com.cynergisuite.middleware.shipping.freight.term

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object FreightTermTypeDataLoader {

   @JvmStatic
   private val freightTermType = listOf(
      FreightTermType(
         id = 1,
         value = "C",
         description = "Collect",
         localizationCode = "collect"
      ),
      FreightTermType(
         id = 2,
         value = "P",
         description = "Prepaid",
         localizationCode = "prepaid"
      )
   )

   @JvmStatic
   fun random() = freightTermType.random()

   @JvmStatic
   fun predefined(): List<FreightTermType> = freightTermType
}

@Singleton
@Requires(env = ["develop", "test"])
class FreightTermTypeDataLoaderService() {
   fun random() = FreightTermTypeDataLoader.random()
   fun predefined() = FreightTermTypeDataLoader.predefined()
}
