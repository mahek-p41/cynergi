package com.cynergisuite.middleware.accounting.account.payable

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "PurchaseOrderNumberRequiredIndicatorType", title = "Purchase order number required indicator type", description = "Purchase order number required indicator type")
data class PurchaseOrderNumberRequiredIndicatorTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Purchase order number required indicator type")
   var value: String,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for purchase order number required indicator")
   var description: String? = null

) {

   constructor(type: PurchaseOrderNumberRequiredIndicatorType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: PurchaseOrderNumberRequiredIndicatorType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
