package com.cynergisuite.middleware.accounting.account.payable

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object DefaultAccountPayableStatusTypeDataLoader {

   @JvmStatic
   private val defaultAccountPayableStatusType = listOf(
      DefaultAccountPayableStatusType(
         id = 1,
         value = "H",
         description = "Hold",
         localizationCode = "hold"
      ),
      DefaultAccountPayableStatusType(
         id = 2,
         value = "O",
         description = "Open",
         localizationCode = "open"
      )
   )

   @JvmStatic
   fun random(): DefaultAccountPayableStatusType {
      return defaultAccountPayableStatusType.random()
   }

   @JvmStatic
   fun predefined(): List<DefaultAccountPayableStatusType> {
      return defaultAccountPayableStatusType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class DefaultAccountPayableStatusTypeDataLoaderService() {
   fun random() = DefaultAccountPayableStatusTypeDataLoader.random()
   fun predefined() = DefaultAccountPayableStatusTypeDataLoader.predefined()
}
