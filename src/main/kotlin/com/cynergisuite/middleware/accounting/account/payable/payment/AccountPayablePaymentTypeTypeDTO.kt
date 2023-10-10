package com.cynergisuite.middleware.accounting.account.payable.payment

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayablePaymentTypeType", title = "Account payable payment type type", description = "Account payable payment type type")
data class AccountPayablePaymentTypeTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Account payable payment type type")
   var value: String,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for account payable payment type")
   var description: String? = null

) {

   constructor(type: AccountPayablePaymentTypeType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: AccountPayablePaymentTypeType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}