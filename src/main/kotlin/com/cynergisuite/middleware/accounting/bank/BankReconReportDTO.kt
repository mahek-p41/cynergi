package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconSummaryDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationReportDTO
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "BankReconReportDTO", title = "Bank Reconciliation Report DTO", description = "Bank reconciliation report dto")
data class BankReconReportDTO(

   @field:Schema(description = "List of bank reconciliations")
   var vendors: MutableList<BankReconciliationReportDTO> = mutableListOf(),

   @field:NotNull
   @field:Schema(description = "Total balance for each bank reconciliation type")
   var reconciliationSummaries: BankReconSummaryDTO? = null
) {
   constructor(entity: BankReconReportEntity) :
      this(
         vendors = entity.vendors!!.asSequence().map { vendorDetailEntity ->
            BankReconciliationReportDTO(vendorDetailEntity)
         }.toMutableList(),
         reconciliationSummaries = BankReconSummaryDTO(entity.reconciliationSummaries)
      )
}
