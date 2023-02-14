package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationReportDetailDTO
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ReconcileBankAccountReportDTO", title = "Reconcile Bank Account Report DTO", description = "Reconcile Bank Account Report DTO")
data class ReconcileBankAccountReportDTO(
   @field:Schema(description = "List of bank reconciliation details")
   var details: List<BankReconciliationReportDetailDTO> = mutableListOf(),
) {
   @get:Schema(description = "Sum of bank reconciliation details amount")
   val sumAmount: BigDecimal get() = details.sumOf { it.amount ?: BigDecimal.ZERO }
}
