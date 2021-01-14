package com.cynergisuite.middleware.purchase.order.type

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "UpdatePurchaseOrderCost", title = "Update purchase order cost", description = "Update purchase order cost type")
data class UpdatePurchaseOrderCostTypeValueObject(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Update purchase order cost code")
   var value: String,

   @field:Nullable
   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for update purchase order cost")
   var description: String? = null

) {

   constructor(type: UpdatePurchaseOrderCostType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: UpdatePurchaseOrderCostType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
