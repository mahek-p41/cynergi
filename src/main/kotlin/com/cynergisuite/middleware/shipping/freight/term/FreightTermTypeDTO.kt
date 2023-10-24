package com.cynergisuite.middleware.shipping.freight.term

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "FreightTermType", title = "Freight Term Type", description = "Freight Term Type")
data class FreightTermTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 15)
   @field:Schema(name = "value", description = "Freight Term Type")
   var value: String? = null,

   @field:Size(min = 3, max = 50)
   @field:Schema(name = "description", description = "Freight Term Description")
   var description: String? = null

) {

   constructor(type: FreightTermType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: FreightTermType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
