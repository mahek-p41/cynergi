package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.extensions.sumByBigDecimal
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerSourceReportTemplate", title = "General Ledger Source Report Template", description = "General Ledger Source Report Template")
data class GeneralLedgerSourceReportTemplate(

   @field:Schema(description = "List of GL source codes included in the report")
   var sourceCodes: List<GeneralLedgerSourceReportSourceDetailDTO>? = null,

   @field:Schema(description = "Total GL detail debit amounts for all source codes")
   var reportTotalDebit: BigDecimal? = null,

   @field:Schema(description = "Total GL detail credit amounts for all source codes")
   var reportTotalCredit: BigDecimal? = null

) {
   constructor(sourceCodes: List<GeneralLedgerSourceReportSourceDetailDTO>) :
      this(
         sourceCodes = sourceCodes,
         reportTotalDebit = sourceCodes.sumByBigDecimal { it.sourceTotalDebit!!.abs() },
         reportTotalCredit = sourceCodes.sumByBigDecimal { it.sourceTotalCredit!!.abs() }
      )
}
