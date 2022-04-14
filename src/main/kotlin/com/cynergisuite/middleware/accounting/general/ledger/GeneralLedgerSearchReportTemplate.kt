package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.extensions.sumByBigDecimal
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailEntity
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "GeneralLedgerSearchReportTemplate", title = "General Ledger Search Report Template", description = "General Ledger Search Report Template")
data class GeneralLedgerSearchReportTemplate(

   @field:Schema(description = "Total credit amount for all General Ledger details")
   var creditTotal: BigDecimal? = null,

   @field:Schema(description = "Total debit amount for all General Ledger details")
   var debitTotal: BigDecimal? = null,

   @field:Schema(description = "Listing of general ledger search reports")
   var payments: List<GeneralLedgerSearchReportDTO>? = null

){
   constructor(entities: List<GeneralLedgerDetailEntity>) :
      this(
         payments = entities.asSequence().map { glEntity ->
            GeneralLedgerSearchReportDTO(glEntity)
         }.toList(),
         creditTotal = entities.filter { it.amount <= BigDecimal.ZERO }.sumByBigDecimal { it.amount },
         debitTotal = entities.filter { it.amount >= BigDecimal.ZERO }.sumByBigDecimal { it.amount }
      )
}
