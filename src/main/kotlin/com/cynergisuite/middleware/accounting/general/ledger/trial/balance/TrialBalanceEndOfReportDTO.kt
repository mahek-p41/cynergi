package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@Schema(name = "TrialBalanceEndOfReport", title = "Trial Balance End Of Report", description = "End of report totals")
data class TrialBalanceEndOfReportDTO(

   @field:NotNull
   @field:Schema(description = "MTD total debits for income & expenses")
   var mtdDebitIE: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "MTD total credits for income & expenses")
   var mtdCreditIE: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "MTD difference of debits and credits for income & expenses")
   var mtdDifferenceIE: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "YTD total debits for income & expenses")
   var ytdDebitIE: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "YTD total credits for income & expenses")
   var ytdCreditIE: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "YTD difference of debits and credits for income & expenses")
   var ytdDifferenceIE: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "MTD total debits for assets & liabilities")
   var mtdDebitAL: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "MTD total credits for assets & liabilities")
   var mtdCreditAL: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "MTD difference of debits and credits for assets & liabilities")
   var mtdDifferenceAL: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "YTD total debits for assets & liabilities")
   var ytdDebitAL: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "YTD total credits for assets & liabilities")
   var ytdCreditAL: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "YTD difference of debits and credits for assets & liabilities")
   var ytdDifferenceAL: BigDecimal? = BigDecimal.ZERO

)
