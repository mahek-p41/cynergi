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
@Schema(name = "StagingDeposit", title = "Staging Deposit DTO", description = "Staging Deposit DTO")
data class StagingDepositDTO(
   var id: UUID,
   var verifySuccessful: Boolean? = null,
   var businessDate: LocalDate? = null,
   var movedToPendingJournalEntries: Boolean? = null,
   var store: Int? = null,
   var storeName: String? = null,
   var errorAmount: BigDecimal? = null,
   var deposit1Cash: BigDecimal? = null,
   var deposit2PmtForOtherStores: BigDecimal? = null,
   var deposit3PmtFromOtherStores: BigDecimal? = null,
   var deposit4CCInStr: BigDecimal? = null,
   var deposit5ACHOLP: BigDecimal? = null,
   var deposit6CCOLP: BigDecimal? = null,
   var deposit7DebitCard: BigDecimal? = null,
   var deposit8ACHChargeback: BigDecimal? = null,
   var deposit9ICCChargeback: BigDecimal? = null,
   var deposit10NSFReturnCheck: BigDecimal? = null,
   var deposit11ARBadCheck: BigDecimal? = null,
   var depositTotal: BigDecimal? = null
) {
   constructor(entity: StagingDepositEntity) : this(
      id = entity.id,
      verifySuccessful = entity.verifySuccessful,
      businessDate = entity.businessDate,
      movedToPendingJournalEntries = entity.movedToPendingJournalEntries,
      store = entity.store,
      storeName = entity.storeName,
      errorAmount = entity.errorAmount,
      deposit1Cash = entity.deposit1Cash,
      deposit2PmtForOtherStores = entity.deposit2PmtForOtherStores,
      deposit3PmtFromOtherStores = entity.deposit3PmtFromOtherStores,
      deposit4CCInStr = entity.deposit4CCInStr,
      deposit5ACHOLP = entity.deposit5ACHOLP,
      deposit6CCOLP = entity.deposit6CCOLP,
      deposit7DebitCard = entity.deposit7DebitCard,
      deposit8ACHChargeback = entity.deposit8ACHChargeback,
      deposit9ICCChargeback = entity.deposit9ICCChargeback,
      deposit10NSFReturnCheck = entity.deposit10NSFReturnCheck,
      deposit11ARBadCheck = entity.deposit11ARBadCheck,
      depositTotal = entity.depositTotal
   )
}
