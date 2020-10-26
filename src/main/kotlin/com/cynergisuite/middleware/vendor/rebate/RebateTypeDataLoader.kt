package com.cynergisuite.middleware.vendor.rebate

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object RebateTypeDataLoader {

   @JvmStatic
   private val rebateType = listOf(
      RebateType(
         id = 1,
         value = "P",
         description = "Percent",
         localizationCode = "percent"
      ),
      RebateType(
         id = 2,
         value = "U",
         description = "Unit",
         localizationCode = "unit"
      )
   )

   @JvmStatic
   fun random(): RebateType {
      return rebateType.random()
   }

   @JvmStatic
   fun predefined(): List<RebateType> {
      return rebateType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class RebateTypeDataLoaderService() {
   fun random() = RebateTypeDataLoader.random()
   fun predefined() = RebateTypeDataLoader.predefined()
}
