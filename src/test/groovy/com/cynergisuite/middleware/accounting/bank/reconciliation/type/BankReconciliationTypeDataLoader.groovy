package com.cynergisuite.middleware.accounting.bank.reconciliation.type

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

class BankReconciliationTypeDataLoader {

   private final static List<BankReconciliationType> bankReconciliationType = [
      new BankReconciliationType(
         1,
         "A",
         "ACH",
         "ach"
      ),
      new BankReconciliationType(
         2,
         "C",
         "Check",
         "check"
      ),
      new BankReconciliationType(
         3,
         "D",
         "Deposit",
         "deposit"
      ),
      new BankReconciliationType(
         4,
         "F",
         "Fee",
         "fee"
      ),
      new BankReconciliationType(
         5,
         "I",
         "Interest",
         "interest"
      ),
      new BankReconciliationType(
         6,
         "M",
         "Miscellaneous",
         "miscellaneous"
      ),
      new BankReconciliationType(
         7,
         "S",
         "Service Charge",
         "service.charge"
      ),
      new BankReconciliationType(
         8,
         "T",
         "Transfer",
         "transfer"
      ),
      new BankReconciliationType(
         9,
         "R",
         "Return Check",
         "return.check"
      ),
      new BankReconciliationType(
         10,
         "V",
         "Void",
         "void"
      )
   ]

   static BankReconciliationType random() {
      return bankReconciliationType.random()
   }

   static List<BankReconciliationType> predefined() {
      return bankReconciliationType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class BankReconciliationTypeDataLoaderService {
   def random() { BankReconciliationTypeDataLoader.random() }
   def predefined() { BankReconciliationTypeDataLoader.predefined() }
}
