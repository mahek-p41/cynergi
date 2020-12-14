package com.cynergisuite.middleware.accounting.general.ledger.recurring

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object GeneralLedgerRecurringTypeDataLoader {

   @JvmStatic
   private val generalLedgerRecurringType = listOf(
      GeneralLedgerRecurringType(
         id = 1,
         value = "D",
         description = "Daily",
         localizationCode = "daily"
      ),
      GeneralLedgerRecurringType(
         id = 2,
         value = "M",
         description = "Monthly",
         localizationCode = "monthly"
      ),
      GeneralLedgerRecurringType(
         id = 3,
         value = "W",
         description = "Weekly",
         localizationCode = "weekly"
      ),
      GeneralLedgerRecurringType(
         id = 4,
         value = "O",
         description = "Other",
         localizationCode = "other"
      )
   )

   @JvmStatic
   fun random(): GeneralLedgerRecurringType {
      return generalLedgerRecurringType.random()
   }

   @JvmStatic
   fun predefined(): List<GeneralLedgerRecurringType> {
      return generalLedgerRecurringType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class GeneralLedgerRecurringTypeDataLoaderService() {
   fun random() = GeneralLedgerRecurringTypeDataLoader.random()
   fun predefined() = GeneralLedgerRecurringTypeDataLoader.predefined()
}
