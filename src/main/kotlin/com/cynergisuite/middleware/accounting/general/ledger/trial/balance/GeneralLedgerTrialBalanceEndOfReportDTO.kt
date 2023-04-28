package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerNetChangeDTO
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@Schema(name = "GeneralLedgerTrialBalanceEndOfReportDTO", title = "General Ledger Trial Balance End Of Report", description = "End of report totals")
data class GeneralLedgerTrialBalanceEndOfReportDTO(
   @field:NotNull
   @field:Schema(description = "List of account detail DTOs")
   private var accounts: List<GeneralLedgerTrialBalanceReportAccountDTO>? = null,
) {
   private val glTotalsIE: List<GeneralLedgerNetChangeDTO> =
      accounts?.filter { it.accountType in setOf("R", "E") }?.mapNotNull { it.glTotals }.orEmpty()

   private val glTotalsAL: List<GeneralLedgerNetChangeDTO> =
      accounts?.filter { it.accountType in setOf("A", "L", "C") }?.mapNotNull { it.glTotals }.orEmpty()

   @field:NotNull
   @field:Schema(description = "MTD total debits for income & expenses")
   val mtdDebitIE: BigDecimal = glTotalsIE.sumOf { it.debit }

   @field:NotNull
   @field:Schema(description = "MTD total credits for income & expenses")
   val mtdCreditIE: BigDecimal = glTotalsIE.sumOf { it.credit }

   @field:NotNull
   @field:Schema(description = "MTD difference of debits and credits for income & expenses")
   val mtdDifferenceIE: BigDecimal = mtdDebitIE + mtdCreditIE

   @field:NotNull
   @field:Schema(description = "YTD total debits for income & expenses")
   val ytdDebitIE: BigDecimal = glTotalsIE.sumOf { it.ytdDebit }

   @field:NotNull
   @field:Schema(description = "YTD total credits for income & expenses")
   val ytdCreditIE: BigDecimal = glTotalsIE.sumOf { it.ytdCredit }

   @field:NotNull
   @field:Schema(description = "YTD difference of debits and credits for income & expenses")
   val ytdDifferenceIE: BigDecimal = ytdDebitIE + ytdCreditIE

   @field:NotNull
   @field:Schema(description = "MTD total debits for assets & liabilities")
   val mtdDebitAL: BigDecimal = glTotalsAL.sumOf { it.debit }

   @field:NotNull
   @field:Schema(description = "MTD total credits for assets & liabilities")
   val mtdCreditAL: BigDecimal = glTotalsAL.sumOf { it.credit }

   @field:NotNull
   @field:Schema(description = "MTD difference of debits and credits for assets & liabilities")
   val mtdDifferenceAL: BigDecimal = mtdDebitAL + mtdCreditAL

   @field:NotNull
   @field:Schema(description = "YTD total debits for assets & liabilities")
   val ytdDebitAL: BigDecimal = glTotalsAL.sumOf { it.ytdDebit }

   @field:NotNull
   @field:Schema(description = "YTD total credits for assets & liabilities")
   val ytdCreditAL: BigDecimal = glTotalsAL.sumOf { it.ytdCredit }

   @field:NotNull
   @field:Schema(description = "YTD difference of debits and credits for assets & liabilities")
   val ytdDifferenceAL: BigDecimal = ytdDebitAL + ytdCreditAL
}
