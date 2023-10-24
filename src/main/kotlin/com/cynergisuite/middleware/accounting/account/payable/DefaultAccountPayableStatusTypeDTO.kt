package com.cynergisuite.middleware.accounting.account.payable

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "DefaultAccountPayableStatusType", title = "Default account payable status type", description = "Default account payable status type")
data class DefaultAccountPayableStatusTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Default account payable status type")
   var value: String,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for default account payable status")
   var description: String? = null

) {

   constructor(type: DefaultAccountPayableStatusType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: DefaultAccountPayableStatusType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
