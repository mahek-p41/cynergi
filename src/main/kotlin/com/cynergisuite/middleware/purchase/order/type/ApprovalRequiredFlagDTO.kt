package com.cynergisuite.middleware.purchase.order.type

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "ApprovalRequiredFlag", title = "Approval required flag", description = "Approval required flag type")
data class ApprovalRequiredFlagDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Approval required flag type")
   var value: String,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for approval required flag")
   var description: String? = null

) {

   constructor(type: ApprovalRequiredFlagType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: ApprovalRequiredFlagType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
