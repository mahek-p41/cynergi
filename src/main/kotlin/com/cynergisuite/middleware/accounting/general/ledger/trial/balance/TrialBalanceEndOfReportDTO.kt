package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@Schema(name = "TrialBalanceEndOfReport", title = "Trial Balance End Of Report", description = "End of report totals")
data class TrialBalanceEndOfReportDTO(

   @field:NotNull
   @field:Schema(description = "MTD total debits")
   var mtdDebit: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "MTD total credits")
   var mtdCredit: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "MTD difference of debits and credits")
   var mtdDifference: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "YTD total debits")
   var ytdDebit: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "YTD total credits")
   var ytdCredit: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "YTD difference of debits and credits")
   var ytdDifference: BigDecimal? = BigDecimal.ZERO

)
