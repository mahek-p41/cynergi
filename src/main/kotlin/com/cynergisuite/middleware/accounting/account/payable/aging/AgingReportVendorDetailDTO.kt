package com.cynergisuite.middleware.accounting.account.payable.aging

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AgingReportVendorDetail", title = "Aging Report Vendor Detail", description = "Vendor detail for AP Aging report")
data class AgingReportVendorDetailDTO(

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

   @field:Schema(description = "List of invoices for the vendor", required = false)
   var invoices: MutableSet<AgingReportInvoiceDetailDTO> = mutableSetOf(),

   @field:Schema(description = "Total balance for the vendor in each balance display column")
   var vendorTotals: BalanceDisplayTotalsDTO? = null

) {
   constructor(entity: AgingReportVendorDetailEntity) :
      this(
         vendorCompanyId = entity.vendorCompanyId,
         vendorNumber = entity.vendorNumber,
         vendorName = entity.vendorName,
         invoices = entity.invoices!!.asSequence().map { invoiceDetailEntity ->
            AgingReportInvoiceDetailDTO(invoiceDetailEntity)
         }.toMutableSet(),
         vendorTotals = BalanceDisplayTotalsDTO(entity.vendorTotals)
      )
}
