package com.cynergisuite.middleware.accounting.account.payable.recurring

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

class ExpenseMonthCreationTypeDataLoader {

   private static final List<ExpenseMonthCreationType> expenseMonthCreationType = [
      new ExpenseMonthCreationType(
         1,
         "C",
         "Current Month",
         "current.month"
      ),
      new ExpenseMonthCreationType(
         2,
         "N",
         "Next Month",
         "next.month"
      )
   ]

   static ExpenseMonthCreationType random() {
      return expenseMonthCreationType.random()
   }

   static List<ExpenseMonthCreationType> predefined() {
      return expenseMonthCreationType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class ExpenseMonthCreationTypeDataLoaderService {
   static random() { ExpenseMonthCreationTypeDataLoader.random() }
   static predefined() { ExpenseMonthCreationTypeDataLoader.predefined() }
}
