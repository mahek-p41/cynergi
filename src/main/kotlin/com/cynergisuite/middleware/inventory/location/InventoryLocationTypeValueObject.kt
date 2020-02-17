package com.cynergisuite.middleware.inventory.location

import com.cynergisuite.domain.ValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValueObject
@JsonInclude(NON_NULL)
@Schema(name = "InventoryLocationType", title = "Inventory Location Type", description = "Defines where an inventory item can live for an AuditDetail or AuditException")
data class InventoryLocationTypeValueObject(

   @field:NotNull
   @field:Size(min = 3, max = 15)
   @field:Schema(name = "value", description = "System interpretable value, most likely all caps", example = "STORE")
   var value: String,

   @field:Size(min = 3, max = 50)
   @field:Schema(name = "description", description = "Human readable description that is good for detailed display", example = "Store")
   var description: String? = null
) {
   constructor(entity: InventoryLocationType, localizedDescription: String) :
      this(
         value = entity.value,
         description = localizedDescription
      )
}
