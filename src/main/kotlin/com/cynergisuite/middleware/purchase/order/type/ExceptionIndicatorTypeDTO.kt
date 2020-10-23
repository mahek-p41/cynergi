package com.cynergisuite.middleware.purchase.order.type

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "ExceptionIndicator", title = "Exception indicator", description = "Exception indicator type")
data class ExceptionIndicatorTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Exception indicator")
   var value: String,

   @field:Nullable
   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for exception indicator")
   var description: String? = null

) {

   constructor(type: ExceptionIndicatorType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: ExceptionIndicatorType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
