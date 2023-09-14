package com.cynergisuite.middleware.accounting.bank.reconciliation

enum class BankReconciliationTypeEnum(val codeValue: String){
   ACH("A"),
   CHECK("C"),
   DEPOSIT("D"),
   FEE("F"),
   INTEREST("I"),
   MISC("M"),
   SERVICE_CHARGE("S"),
   TRANSFER("T"),
   RETURN_CHECK("R"),
   VOID("V")
}
