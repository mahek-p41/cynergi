package com.cynergisuite.middleware.purchase.order.type

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "DefaultPurchaseOrderType", title = "Default purchase order type", description = "Default purchase order type")
data class DefaultPurchaseOrderTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Default purchase order code")
   var value: String,

   @field:Nullable
   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for default purchase order")
   var description: String? = null

) {

   constructor(type: DefaultPurchaseOrderType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: DefaultPurchaseOrderType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
