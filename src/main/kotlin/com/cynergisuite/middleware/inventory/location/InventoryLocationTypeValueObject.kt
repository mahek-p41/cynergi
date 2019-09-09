package com.cynergisuite.middleware.inventory.location

import com.cynergisuite.domain.ValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValueObject
@JsonInclude(NON_NULL)
data class InventoryLocationTypeValueObject(

   @field:NotNull
   @field:Size(min = 3, max = 15)
   var value: String,

   @field:Size(min = 3, max = 50)
   var description: String? = null
) {
   constructor(entity: InventoryLocationType, localizedDescription: String) :
      this(
         value = entity.value,
         description = localizedDescription
      )
}
