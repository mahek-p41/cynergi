package com.cynergisuite.middleware.vendor

import com.cynergisuite.middleware.address.AddressDTO
import com.cynergisuite.middleware.company.CompanyDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "Form1099ReportDTO", title = "Vendor Form 1099 Report DTO", description = "Vendor Form 1099 Report DTO")
data class Form1099ReportDTO(

   @field:NotNull
   @field:Schema(name = "companyName", required = false, nullable = false, description = "Human readable name for a company")
   var companyName: String? = null,

   @field:Schema(description = "List of vendors")
   var vendors: MutableList<Form1099VendorDTO> = mutableListOf(),

   @field:NotNull
   @field:Schema(description = "Total balance for each date range column")
   var reportTotals: Form1099TotalsDTO? = null
) {
   constructor(entity: Form1099ReportEntity) :
      this(
         companyName = entity.companyName,
         vendors = entity.vendors!!.asSequence().map { vendorDetailEntity ->
            Form1099VendorDTO(vendorDetailEntity)
         }.toMutableList(),
         reportTotals = Form1099TotalsDTO(entity.reportTotals)
      )
}
