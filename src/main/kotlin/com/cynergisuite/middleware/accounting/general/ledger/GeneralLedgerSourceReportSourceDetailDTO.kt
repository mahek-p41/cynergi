package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.extensions.sumByBigDecimal
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailEntity
import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "GeneralLedgerSourceReportSourceDetail", title = "General Ledger Source Report Source Detail", description = "General Ledger Source Report Source Detail")
data class GeneralLedgerSourceReportSourceDetailDTO(

   @field:NotNull
   @field:Schema(description = "Source code")
   var sourceCode: GeneralLedgerSourceCodeDTO? = null,

   @field:Schema(description = "List of payment details for the source code")
   var details: List<GeneralLedgerSourceReportDetailDTO>? = null,

   @field:Schema(description = "Total GL detail debit amounts for the source code")
   var sourceTotalDebit: BigDecimal? = null,

   @field:Schema(description = "Total GL detail credit amounts for the source code")
   var sourceTotalCredit: BigDecimal? = null

) {
   constructor(entities: List<GeneralLedgerDetailEntity>) :
      this(
         sourceCode = GeneralLedgerSourceCodeDTO(entities[0].source),
         details = entities.asSequence().map { glEntity ->
            GeneralLedgerSourceReportDetailDTO(glEntity)
         }.toList(),
         sourceTotalDebit = entities.filter { it.amount >= BigDecimal.ZERO }.sumByBigDecimal { it.amount },
         sourceTotalCredit = entities.filter { it.amount <= BigDecimal.ZERO }.sumByBigDecimal { it.amount }
      )
}
