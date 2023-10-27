package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.extensions.sumByBigDecimal
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "GeneralLedgerPendingReportTemplate", title = "General Ledger Pending Report Template", description = "General Ledger Pending Report Template")
data class GeneralLedgerPendingReportTemplate(

   @field:Schema(description = "Total credit amount for all General Ledger details")
   var creditTotal: BigDecimal? = null,

   @field:Schema(description = "Total debit amount for all General Ledger details")
   var debitTotal: BigDecimal? = null,

   @field:Schema(description = "Listing of general ledger search reports")
   var payments: List<GeneralLedgerPendingReportDetailsTemplate>? = null

){
   constructor(entities: List<GeneralLedgerPendingReportDetailsTemplate> ) :
      this(
         payments = entities,
         creditTotal = entities.sumByBigDecimal { it.creditSubtotal!! },
         debitTotal = entities.sumByBigDecimal { it.debitSubtotal!! }
      )
}
