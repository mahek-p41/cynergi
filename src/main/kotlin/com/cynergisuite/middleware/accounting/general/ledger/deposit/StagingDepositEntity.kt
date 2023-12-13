package com.cynergisuite.middleware.accounting.general.ledger.deposit

import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Introspected
data class StagingDepositEntity(
   val id: UUID,
   val verifySuccessful: Boolean,
   val businessDate: LocalDate,
   val movedToPendingJournalEntries: Boolean,
   val store: Int,
   val storeName: String,
   val errorAmount: BigDecimal,
   val deposit1Cash: BigDecimal,
   val deposit2PmtForOtherStores: BigDecimal,
   val deposit3PmtFromOtherStores: BigDecimal,
   val deposit4CCInStr: BigDecimal,
   val deposit5ACHOLP: BigDecimal,
   val deposit6CCOLP: BigDecimal,
   val deposit7DebitCard: BigDecimal,
   val deposit8ACHChargeback: BigDecimal?,
   val deposit9ICCChargeback: BigDecimal?,
   val deposit10NSFReturnCheck: BigDecimal?,
   val deposit11ARBadCheck: BigDecimal?,
   val depositTotal: BigDecimal
)
