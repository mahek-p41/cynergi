package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.extensions.sumByBigDecimal
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "GeneralLedgerPendingJournalCountDTO", title = "General Ledger Pending Journal Count DTO", description = "General Ledger Pending Journal Count DTO")
data class GeneralLedgerPendingJournalCountDTO(


   @field:Schema(description = "Balance of debit and credit for all General Ledger Pending Journal Entries")
   var balance: BigDecimal? = null,

   @field:Schema(description = "Total debit amount for all General Ledger Pending Journal Entries")
   var debitTotal: BigDecimal? = null,

   @field:Schema(description = "Total credit amount for all General Ledger Pending Journal Entries")
   var creditTotal: BigDecimal? = null,

   @field:Schema(description = "Number of Journal Entries")
   var jeCount: Int? = null

){
   constructor(entities: List<GeneralLedgerJournalEntity>, balance: BigDecimal ) :
      this(
         balance = balance,
         debitTotal = entities.filter { it.amount >= BigDecimal.ZERO }.sumByBigDecimal { it.amount },
         creditTotal = entities.filter { it.amount < BigDecimal.ZERO }.sumByBigDecimal { it.amount },
         jeCount = entities.size
      )
}
