package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconSummaryDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationReportDetailDTO
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "BankReconciliationReportDTO", title = "Bank Reconciliation Report DTO", description = "Bank reconciliation report dto")
data class BankReconciliationReportDTO(

   @field:Schema(description = "List of bank reconciliation details")
   var vendors: MutableList<BankReconciliationReportDetailDTO> = mutableListOf(),

   @field:NotNull
   @field:Schema(description = "Total balance for each bank reconciliation type")
   var reconciliationSummaries: BankReconSummaryDTO? = null
) {
   constructor(entity: BankReconciliationReportEntity) :
      this(
         vendors = entity.vendors!!.asSequence().map { vendorDetailEntity ->
            BankReconciliationReportDetailDTO(vendorDetailEntity)
         }.toMutableList(),
         reconciliationSummaries = BankReconSummaryDTO(entity.reconciliationSummaries)
      )
}
