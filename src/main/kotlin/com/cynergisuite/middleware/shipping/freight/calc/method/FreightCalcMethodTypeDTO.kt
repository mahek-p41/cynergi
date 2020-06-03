package com.cynergisuite.middleware.shipping.freight.calc.method

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "FreightCalcMethodType", title = "Freight Method Type", description = "Freight Method Type")
data class FreightCalcMethodTypeDTO (

   @field:NotNull
   @field:Size(min = 1, max = 15)
   @field:Schema(name = "value", description = "Freight Method Type")
   var value: String? = null,

   @field:Size(min = 3, max = 50)
   @field:Schema(name="description", description = "Freight Method Description")
   var description: String? = null

) {

   constructor(freightCalcMethodType: FreightCalcMethodType) :
      this(
         value = freightCalcMethodType.value,
         description = freightCalcMethodType.description
      )

   constructor(freightCalcMethodType: FreightCalcMethodType, localizedDescription: String) :
      this(
         value = freightCalcMethodType.value,
         description = localizedDescription
      )
}
