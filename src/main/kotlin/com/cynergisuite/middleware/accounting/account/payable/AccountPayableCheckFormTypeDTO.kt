package com.cynergisuite.middleware.accounting.account.payable

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableCheckForm", title = "Account payable check form", description = "Account payable check form type")
data class AccountPayableCheckFormTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Account payable check form type")
   var value: String,

   @field:Nullable
   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for account payable check form")
   var description: String? = null

) {

   constructor(type: AccountPayableCheckFormType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: AccountPayableCheckFormType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
