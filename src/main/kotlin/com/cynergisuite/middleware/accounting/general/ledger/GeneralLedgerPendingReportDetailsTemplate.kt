package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.extensions.sumByBigDecimal
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "GeneralLedgerPendingReportDetailsTemplate", title = "General Ledger Pending Report Details Template", description = "General Ledger Pending Report Details Template")
data class GeneralLedgerPendingReportDetailsTemplate(

   @field:Schema(description = "Subtotal credit amount for sorted General Ledger journal")
   var creditSubtotal: BigDecimal? = null,

   @field:Schema(description = "Subtotal debit amount for all General Ledger joournal")
   var debitSubtotal: BigDecimal? = null,

   @field:Schema(description = "Listing of general ledger pending reports")
   var payments: List<GeneralLedgerPendingReportDTO>? = null

){
   constructor(entities: List<GeneralLedgerJournalEntity> ) :
      this(
         payments = entities.asSequence().map { glEntity ->
            GeneralLedgerPendingReportDTO(glEntity)
         }.toList(),
         creditSubtotal = entities.filter { it.amount <= BigDecimal.ZERO }.sumByBigDecimal { it.amount },
         debitSubtotal = entities.filter { it.amount >= BigDecimal.ZERO }.sumByBigDecimal { it.amount }
      )
}
