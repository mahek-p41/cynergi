package com.cynergisuite.middleware.accounting.general.ledger.deposit

import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Introspected
data class AccountingDetailDTO(
   val verifyId: UUID,
   val accountId: UUID,
   val accountNumber: Int,
   val accountName: String,
   val profitCenterNumber: Int,
   val sourceId: UUID,
   val sourceValue: String,
   val debit: BigDecimal,
   val credit: BigDecimal,
   val message: String?,
   val date: LocalDate
)
