package com.cynergisuite.middleware.accounting.account.payable.aging

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableAgingReport", title = "Account Payable Aging Report", description = "Account payable aging report")
data class AccountPayableAgingReportDTO(

   @field:Schema(description = "List of vendors")
   var vendors: MutableList<AgingReportVendorDetailDTO> = mutableListOf(),

   @field:NotNull
   @field:Schema(description = "Total balance for the report in each balance display column")
   var agedTotals: BalanceDisplayTotalsDTO? = null

) {
   constructor(entity: AccountPayableAgingReportEntity) :
      this(
         vendors = entity.vendors!!.asSequence().map { vendorDetailEntity ->
            AgingReportVendorDetailDTO(vendorDetailEntity)
         }.toMutableList(),
         agedTotals = BalanceDisplayTotalsDTO(entity.agedTotals)
      )
}
