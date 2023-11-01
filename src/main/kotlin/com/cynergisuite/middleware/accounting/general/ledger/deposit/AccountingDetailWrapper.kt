package com.cynergisuite.middleware.accounting.general.ledger.deposit

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema

@Introspected
data class AccountingDetailWrapper(
   val accountingDetails: List<AccountingDetailDTO>,
) {
   @get:Schema(description = "Total debit amount for all General Ledger Accounting Entries")
   val debitTotal get() = accountingDetails.map { it.debit }.sumOf { it }

   @get:Schema(description = "Total credit amount for all General Ledger Accounting Entries")
   val creditTotal get() = accountingDetails.map { it.credit }.sumOf { it }
}
