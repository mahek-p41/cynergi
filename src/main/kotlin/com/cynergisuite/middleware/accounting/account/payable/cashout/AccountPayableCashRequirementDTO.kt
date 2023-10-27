package com.cynergisuite.middleware.accounting.account.payable.cashout

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableCashRequirementReportDTO", title = "Account Payable Cash Requirement Report DTO", description = "Account Payable cash requirement report dto")
data class AccountPayableCashRequirementDTO(

   @field:Schema(description = "List of vendors")
   var vendors: MutableList<CashRequirementVendorDTO> = mutableListOf(),

   @field:NotNull
   @field:Schema(description = "Total balance for each date range column")
   var cashoutTotals: CashRequirementBalanceDTO? = null
) {
   constructor(entity: AccountPayableCashRequirementEntity) :
      this(
         vendors = entity.vendors!!.asSequence().map { vendorDetailEntity ->
            CashRequirementVendorDTO(vendorDetailEntity)
         }.toMutableList(),
         cashoutTotals = CashRequirementBalanceDTO(entity.cashoutTotals)
      )
}
