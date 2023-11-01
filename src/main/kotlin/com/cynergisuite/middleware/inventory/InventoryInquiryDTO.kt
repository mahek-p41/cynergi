package com.cynergisuite.middleware.inventory

import com.fasterxml.jackson.annotation.JsonView
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Introspected
@JsonView
@Schema(name = "InventoryInquiry", title = "Inventory Inquiry", description = "Account payable inventory inquiry")
data class InventoryInquiryDTO(

   @field:Schema(name = "modelNumber", description = "Manufacturer defined model number")
   var modelNumber: String? = null,

   @field:Schema(name = "serialNumber", description = "Either a manufacturer defined serial number or a system generated serial number")
   var serialNumber: String? = null,

   @field:Schema(name = "landedCost", description = "Cost of item currently (actual cost)")
   var landedCost: BigDecimal? = null,

   @field:Schema(name = "status", description = "Status of referenced inventory item")
   var status: String? = null,

   @field:Schema(name = "receivedDate", description = "Date item was received into inventory")
   var receivedDate: LocalDate? = null,

   @field:Schema(name = "poNbr", description = "Purchase order number")
   var poNbr: String? = null,

   @field:Schema(name = "invoiceNbr", description = "Invoice number")
   var invoiceNbr: String? = null,

   @field:Schema(name = "description", description = "Describes the referenced inventory item")
   var description: String? = null,

   @field:Schema(name = "currentLoc", description = "Current location that the referenced inventory item is stored at")
   var currentLoc: Int? = null,

   @field:Schema(name = "invoiceExpensedDate", description = "Invoice expensed date")
   var invoiceExpensedDate: LocalDate? = null,

   @field:Schema(name = "altId", description = "Alternate Identifier.  One possible value for lookupKey")
   var altId: String? = null,

   @field:Schema(name = "currentLoc", description = "Current location that the referenced inventory item is stored at")
   var currentLocExpensed: String? = null

)
