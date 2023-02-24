package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationReportDetailDTO
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ReconcileBankAccountReportTemplate", title = "Bank Reconciliation Report Template", description = "Bank reconciliation report template")
data class ReconcileBankAccountReportTemplate(
   @Schema(description = "List of input bank reconciliations")
   private val reconciliations: List<BankReconciliationReportDetailDTO>,
   @Schema(description = "GL Balance")
   private val glBalance: BigDecimal
) {
   @field:Schema(description = "List of bank reconciliations grouped by type")
   val groupedReconciliations: List<ReconcileBankAccountReportDTO> = reconciliations.groupBy { it.type }
      .map { (_, list) -> ReconcileBankAccountReportDTO(list) }

   @field:Schema(description = "Sum of all outstanding items amount")
   val totalOutstandingItems: BigDecimal = reconciliations.sumOf { it.amount ?: BigDecimal.ZERO }

   @get:Schema(description = "Computed Bank Statement Balance")
   val computedBankStmtBalance: BigDecimal get() = glBalance
      .minus(totalOutstandingItems)
      .add(groupedReconciliations.firstOrNull { it.details.firstOrNull()?.type?.value == "V" }?.sumAmount ?: BigDecimal.ZERO)
}
