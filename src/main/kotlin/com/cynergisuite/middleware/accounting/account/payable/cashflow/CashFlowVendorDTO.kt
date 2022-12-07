package com.cynergisuite.middleware.accounting.account.payable.cashflow

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class CashFlowVendorDTO (

@field:NotNull
@field:Schema(description = "Vendor company id")
var vendorCompanyId: UUID? = null,

@field:Positive
@field:Schema(description = "Vendor number")
var vendorNumber: Int? = null,

@field:NotNull
@field:NotBlank
@field:Size(min = 1, max = 30)
@field:Schema(description = "Vendor name")
var vendorName: String? = null,

@field:NotNull
@field:Schema(description = "Invoices")
var invoices: MutableSet<CashFlowReportInvoiceDetailEntity>? = LinkedHashSet(),

@field:Schema(description = "Total balance for the vendor in each balance display column")
var vendorTotals: CashFlowBalanceDTO? = null

) {
   constructor(entity: CashFlowVendorEntity) :
      this(
         vendorCompanyId = entity.vendorCompanyId,
         vendorNumber = entity.vendorNumber,
         vendorName = entity.vendorName,
         invoices = entity.invoices,
         vendorTotals = CashFlowBalanceDTO(entity.vendorTotals)
      )
}
