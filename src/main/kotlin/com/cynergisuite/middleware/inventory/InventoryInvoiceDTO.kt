package com.cynergisuite.middleware.inventory

import com.fasterxml.jackson.annotation.JsonView
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import org.jetbrains.annotations.NotNull

@Introspected
@JsonView
@Schema(name = "InventoryInvoiceDTO", title = "Inventory Invoice", description = "Inventory invoice")
class InventoryInvoiceDTO {

   @field:NotNull
   @field:Schema(name = "invoiceId", description = "Invoice ID")
   val invoiceId: UUID? = null

   @field:NotNull
   @field:Schema(name = "invoiceNumber", description = "Invoice number")
   val invoiceNumber: String? = null
}
