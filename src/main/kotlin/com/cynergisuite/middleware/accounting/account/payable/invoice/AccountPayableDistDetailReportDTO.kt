package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "AccountPayableDistDetailReportDTO", title = "Account Payable Distribution Detail Report", description = "Account Payable Distribution Detail Report")
data class AccountPayableDistDetailReportDTO(
   @field:NotNull
   @field:Schema(description = "Account number")
   var accountNumber: Int? = null,
   @field:Schema(description = "Account name")
   var accountName: String? = null,
   @field:Schema(description = "Distribution store number")
   var distProfitCenter: Int? = null,
   @field:Schema(description = "Distribution amount")
   var distAmount: BigDecimal? = null,
   @field:Schema(description = "GL Account for Inventory indicator")
   var isAccountForInventory: Boolean? = null,
   )
