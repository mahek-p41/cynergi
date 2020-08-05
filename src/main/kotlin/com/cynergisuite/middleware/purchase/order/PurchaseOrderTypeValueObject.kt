package com.cynergisuite.middleware.purchase.order

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "PurchaseOrderType", title = "Purchase order type", description = "Purchase order type")
data class PurchaseOrderTypeValueObject(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Purchase order code")
   var value: String,

   @field:Nullable
   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for purchase order")
   var description: String? = null

) {

   constructor(type: PurchaseOrderType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: PurchaseOrderType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
