package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.extensions.sumByBigDecimal
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailEntity
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "GeneralLedgerDetailPurgeCountDTO", title = "General Ledger Detail Count DTO", description = "General Ledger Detail Count DTO")
data class GeneralLedgerDetailPurgeCountDTO(

   @field:Schema(description = "Balance of debit and credit for the selected General Ledger Detail Entries")
   var balance: BigDecimal? = null,

   @field:Schema(description = "Total debit amount for the selected General Ledger Detail Entries")
   var debitTotal: BigDecimal? = null,

   @field:Schema(description = "Total credit amount for the selected General Ledger Detail Entries")
   var creditTotal: BigDecimal? = null,

   @field:Schema(description = "Number of selected General Ledger Detail Entries")
   var gldCount: Int? = null

){
   constructor(entities: List<GeneralLedgerDetailEntity>, balance: BigDecimal ) :
      this(
         balance = balance,
         debitTotal = entities.filter { it.amount >= BigDecimal.ZERO }.sumByBigDecimal { it.amount },
         creditTotal = entities.filter { it.amount < BigDecimal.ZERO }.sumByBigDecimal { it.amount },
         gldCount = entities.size
      )
}
