package com.cynergisuite.middleware.inventory

import com.fasterxml.jackson.annotation.JsonView
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import org.jetbrains.annotations.NotNull

@Introspected
@JsonView
@Schema(name = "InventoryInvoiceDTO", title = "Inventory Invoice", description = "Inventory invoice")
class AssociateInventoryToInvoiceDTO {

   @field:NotNull
   @field:Schema(name = "attach", description = "List of inventory ids to attach to invoice.")
   val attach: List<UUID>? = null

   @field:NotNull
   @field:Schema(name = "detach", description = "List of inventory ids to detach from invoice.")
   val detach: List<UUID>? = null
}
