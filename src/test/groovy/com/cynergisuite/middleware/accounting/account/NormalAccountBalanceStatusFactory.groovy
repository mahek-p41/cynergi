package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.middleware.accounting.account.normalAccountBalance.NormalAccountBalanceType
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

class NormalAccountBalanceFactory {

   private static List<NormalAccountBalanceType> normalAccountBalances = [
      new NormalAccountBalanceType(
         1,
         "C",
         "Credit",
         "credit"
      ),
      new NormalAccountBalanceType(
         2,
         "D",
         "Debit",
         "debit"
      )
   ]

   static NormalAccountBalanceType random() {
      return normalAccountBalances.random()
   }

   static List<NormalAccountBalanceType> predefined() {
      return normalAccountBalances
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class NormalAccountBalanceFactoryService {

   NormalAccountBalanceType random() {
     return NormalAccountBalanceFactory.random()
   }

   List<NormalAccountBalanceType> predefined() {
      return NormalAccountBalanceFactory.predefined()
   }
}
