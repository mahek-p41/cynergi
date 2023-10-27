package com.cynergisuite.middleware.accounting.general.ledger.deposit

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "StagingDepositStatus", title = "Staging Deposit Status DTO", description = "Staging Deposit Status DTO")
data class StagingDepositStatusDTO(
   var id: UUID,
   var businessDate: LocalDate? = null,
   var store: Int? = null,
   var storeName: String? = null,
   var verifySuccessful: Boolean? = null,
   var movedToPendingJournalEntries: Boolean? = null,
) {
   constructor(entity: StagingDepositEntity) : this(
      id = entity.id,
      verifySuccessful = entity.verifySuccessful,
      businessDate = entity.businessDate,
      movedToPendingJournalEntries = entity.movedToPendingJournalEntries,
      store = entity.store,
      storeName = entity.storeName,
   )
}
