package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationReportDetailDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.json.view.Full
import com.cynergisuite.middleware.json.view.Summary
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@JsonView
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
   name = "ReconcileBankAccountReportDTO",
   title = "Reconcile Bank Account Report DTO",
   description = "Reconcile Bank Account Report DTO"
)
data class ReconcileBankAccountReportDTO(
   @field:JsonView(value = [Full::class, Summary::class])
   @field:Schema(description = "List of bank reconciliation details")
   val type: BankReconciliationTypeDTO?,

   @field:JsonView(value = [Full::class])
   @field:Schema(description = "List of bank reconciliation details")
   var details: List<BankReconciliationReportDetailDTO> = mutableListOf(),
) {
   @get:JsonView(value = [Full::class, Summary::class])
   @get:Schema(description = "Sum of bank reconciliation details amount")
   val sumAmount: BigDecimal get() = details.sumOf { it.amount ?: BigDecimal.ZERO }
}
