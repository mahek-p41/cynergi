package com.cynergisuite.middleware.accounting.general.ledger.deposit

import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Introspected
data class AccountingEntriesStagingEntity(
   val id: UUID,
   val companyId: UUID,
   val accountId: UUID,
   val profitCenter: Int,
   val date: LocalDate,
   val sourceId: UUID,
   val amount: BigDecimal,
   val message: String?
)
