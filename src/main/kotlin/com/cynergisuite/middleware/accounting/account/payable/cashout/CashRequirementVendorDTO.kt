package com.cynergisuite.middleware.accounting.account.payable.cashout

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "CashRequirementVendorDTO", title = "Cash Requirement Vendor Detail", description = "Vendor details for Cash Requirement Report")
data class CashRequirementVendorDTO(

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
   var invoices: MutableSet<CashRequirementReportInvoiceDetailDTO>? = LinkedHashSet(),

   @field:Schema(description = "Total balance for the vendor in each balance display column")
   var vendorTotals: CashRequirementBalanceDTO? = null

) {
   constructor(entity: CashRequirementVendorEntity) :
      this(
         vendorCompanyId = entity.vendorCompanyId,
         vendorNumber = entity.vendorNumber,
         vendorName = entity.vendorName,
         invoices = entity.invoices!!.asSequence().map {
            CashRequirementReportInvoiceDetailDTO(it)
         }.toMutableSet(),
         vendorTotals = CashRequirementBalanceDTO(entity.vendorTotals)
      )
}
