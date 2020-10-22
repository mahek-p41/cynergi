package com.cynergisuite.middleware.accounting.account.payable.recurring

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object ExpenseMonthCreationTypeDataLoader {

   @JvmStatic
   private val expenseMonthCreationType = listOf(
      ExpenseMonthCreationType(
         id = 1,
         value = "C",
         description = "Current Month",
         localizationCode = "current.month"
      ),
      ExpenseMonthCreationType(
         id = 2,
         value = "N",
         description = "Next Month",
         localizationCode = "next.month"
      )
   )

   @JvmStatic
   fun random(): ExpenseMonthCreationType {
      return expenseMonthCreationType.random()
   }

   @JvmStatic
   fun predefined(): List<ExpenseMonthCreationType> {
      return expenseMonthCreationType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class ExpenseMonthCreationTypeDataLoaderService() {
   fun random() = ExpenseMonthCreationTypeDataLoader.random()
   fun predefined() = ExpenseMonthCreationTypeDataLoader.predefined()
}
