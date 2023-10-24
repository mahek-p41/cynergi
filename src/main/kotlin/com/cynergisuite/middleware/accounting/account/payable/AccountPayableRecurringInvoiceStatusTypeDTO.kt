package com.cynergisuite.middleware.accounting.account.payable

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableRecurringInvoiceStatusType", title = "Account payable recurring invoice status", description = "Account payable recurring invoice status type")
data class AccountPayableRecurringInvoiceStatusTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Account payable recurring invoice status type")
   var value: String,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for account payable recurring invoice status")
   var description: String? = null

) {

   constructor(type: AccountPayableRecurringInvoiceStatusType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: AccountPayableRecurringInvoiceStatusType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
