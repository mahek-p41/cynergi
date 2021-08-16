package com.cynergisuite.middleware.purchase.order.type

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "PurchaseOrderRequisitionIndicatorType", title = "Purchase order requisition indicator type", description = "Purchase order requisition indicator type")
data class PurchaseOrderRequisitionIndicatorTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Purchase order requisition indicator")
   var value: String,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for purchase order requisition indicator")
   var description: String? = null

) {

   constructor(type: PurchaseOrderRequisitionIndicatorType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: PurchaseOrderRequisitionIndicatorType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
