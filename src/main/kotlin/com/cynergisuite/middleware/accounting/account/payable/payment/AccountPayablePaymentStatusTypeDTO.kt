package com.cynergisuite.middleware.accounting.account.payable.payment

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableStatus", title = "Account payable status", description = "Account payable status type")
data class AccountPayablePaymentStatusTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Account payable status type")
   var value: String,

   @field:Nullable
   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for account payable status")
   var description: String? = null

) {

   constructor(type: AccountPayablePaymentStatusType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: AccountPayablePaymentStatusType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
