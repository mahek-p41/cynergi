package com.cynergisuite.middleware.purchase.order.type

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "PurchaseOrderStatus", title = "Purchase order status", description = "Purchase order status type")
data class PurchaseOrderStatusTypeValueObject(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Purchase order status code")
   var value: String,

   @field:Nullable
   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for purchase order status")
   var description: String? = null

) {

   constructor(type: PurchaseOrderStatusType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: PurchaseOrderStatusType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
