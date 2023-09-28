package com.cynergisuite.middleware.accounting.account.payable

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableInvoiceType", title = "Account payable invoice", description = "Account payable invoice type")
data class AccountPayableInvoiceTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Account payable invoice type")
   var value: String,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for account payable invoice")
   var description: String? = null

) {

   constructor(type: AccountPayableInvoiceType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: AccountPayableInvoiceType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
