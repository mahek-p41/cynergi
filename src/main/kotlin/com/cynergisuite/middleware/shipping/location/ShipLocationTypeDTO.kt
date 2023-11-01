package com.cynergisuite.middleware.shipping.location

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "ShipLocationType", title = "Ship Location Type", description = "Ship Location Type")
data class ShipLocationTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 15)
   @field:Schema(name = "value", description = "Ship Location Type")
   var value: String? = null,

   @field:Size(min = 3, max = 50)
   @field:Schema(name = "description", description = "Ship Location Description")
   var description: String? = null

) {

   constructor(type: ShipLocationType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: ShipLocationType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
