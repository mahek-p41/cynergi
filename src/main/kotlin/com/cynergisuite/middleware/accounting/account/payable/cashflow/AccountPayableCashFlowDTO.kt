package com.cynergisuite.middleware.accounting.account.payable.cashflow

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableCashFlowReportDTO", title = "Account Payable Cash Flow Report DTO", description = "Account Payable cash flow report dto")
data class AccountPayableCashFlowDTO(

   @field:Schema(description = "List of vendors")
   var vendors: MutableList<CashFlowVendorDTO> = mutableListOf(),

   @field:NotNull
   @field:Schema(description = "Total balance for each date range column")
   var cashflowTotals: CashFlowBalanceDTO? = null
) {
   constructor(entity: AccountPayableCashFlowEntity) :
      this(
         vendors = entity.vendors!!.asSequence().map { vendorDetailEntity ->
            CashFlowVendorDTO(vendorDetailEntity)
         }.toMutableList(),
         cashflowTotals = CashFlowBalanceDTO(entity.cashflowTotals)
      )
}
