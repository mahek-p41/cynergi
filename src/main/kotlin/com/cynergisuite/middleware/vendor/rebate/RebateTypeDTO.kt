package com.cynergisuite.middleware.vendor.rebate

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "RebateTypeDTO", title = "Rebate type", description = "Rebate type")
data class RebateTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Rebate type")
   var value: String,

   @field:Nullable
   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for rebate type")
   var description: String? = null

) {

   constructor(type: RebateType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: RebateType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
