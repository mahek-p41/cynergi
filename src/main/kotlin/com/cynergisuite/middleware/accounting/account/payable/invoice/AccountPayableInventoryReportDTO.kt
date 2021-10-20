package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "AccountPayableInventoryReportDTO", title = "Account Payable Inventory Report", description = "Account Payable Inventory Report")
data class AccountPayableInventoryReportDTO(
   @field:NotNull
   @field:Schema(description = "AP receive date")
   var modelNumber: String? = null,
   var serialNumber: String? = null,
   var description: String? = null,
   var cost: String? = null,
   var received: String? = null,
   var status: String? = null,
   var receivedLocation: String? = null,
   )
