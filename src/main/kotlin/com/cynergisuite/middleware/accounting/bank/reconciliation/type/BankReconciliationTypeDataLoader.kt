package com.cynergisuite.middleware.accounting.bank.reconciliation.type

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object BankReconciliationTypeDataLoader {

   @JvmStatic
   private val bankReconciliationType = listOf(
      BankReconciliationType(
         id = 1,
         value = "C",
         description = "Check",
         localizationCode = "check"
      ),
      BankReconciliationType(
         id = 2,
         value = "D",
         description = "Deposit",
         localizationCode = "deposit"
      ),
      BankReconciliationType(
         id = 3,
         value = "F",
         description = "Fee",
         localizationCode = "fee"
      ),
      BankReconciliationType(
         id = 4,
         value = "I",
         description = "Interest",
         localizationCode = "interest"
      ),
      BankReconciliationType(
         id = 5,
         value = "M",
         description = "Miscellaneous",
         localizationCode = "miscellaneous"
      ),
      BankReconciliationType(
         id = 6,
         value = "S",
         description = "Service Charge",
         localizationCode = "service.charge"
      ),
      BankReconciliationType(
         id = 7,
         value = "T",
         description = "Transfer",
         localizationCode = "transfer"
      ),
      BankReconciliationType(
         id = 8,
         value = "R",
         description = "Return Check",
         localizationCode = "return.check"
      ),
      BankReconciliationType(
         id = 9,
         value = "V",
         description = "Void",
         localizationCode = "void"
      )
   )

   @JvmStatic
   fun random(): BankReconciliationType {
      return bankReconciliationType.random()
   }

   @JvmStatic
   fun predefined(): List<BankReconciliationType> {
      return bankReconciliationType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class BankReconciliationTypeDataLoaderService() {
   fun random() = BankReconciliationTypeDataLoader.random()
   fun predefined() = BankReconciliationTypeDataLoader.predefined()
}
