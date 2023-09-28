package com.cynergisuite.middleware.accounting.general.ledger.recurring

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

class GeneralLedgerRecurringTypeDataLoader {

   private static final List<GeneralLedgerRecurringType> generalLedgerRecurringType = [
      new GeneralLedgerRecurringType(
         1,
         "D",
         "Daily",
         "daily"
      ),
      new GeneralLedgerRecurringType(
         2,
         "M",
         "Monthly",
         "monthly"
      ),
      new GeneralLedgerRecurringType(
         3,
         "W",
         "Weekly",
         "weekly"
      ),
      new GeneralLedgerRecurringType(
         4,
         "O",
         "Other",
         "other"
      )
   ]

   static GeneralLedgerRecurringType random() {
      return generalLedgerRecurringType.random()
   }

   static List<GeneralLedgerRecurringType> predefined() {
      return generalLedgerRecurringType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class GeneralLedgerRecurringTypeDataLoaderService {
   def random() { GeneralLedgerRecurringTypeDataLoader.random() }
   def predefined() { GeneralLedgerRecurringTypeDataLoader.predefined() }
}
