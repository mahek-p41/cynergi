package com.cynergisuite.middleware.accounting.general.ledger.deposit

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "AccountEntriesStaging", title = "Staging Deposit DTO", description = "Staging Deposit DTO")
data class AccountingEntriesStagingDTO(
   var id: UUID? = null,
   var companyId: UUID? = null,
   var accountId: UUID? = null,
   var profitCenter: Int? = null,
   var date: LocalDate? = null,
   var sourceId: UUID? = null,
   var amount: BigDecimal? = null,
   var message: String? = null
) {
   constructor(entity: AccountingEntriesStagingEntity) : this(
      companyId = entity.companyId,
      accountId = entity.accountId,
      profitCenter = entity.profitCenter,
      date = entity.date,
      sourceId = entity.sourceId,
      amount = entity.amount,
      message = entity.message
   )
}
