package com.cynergisuite.middleware.accounting.account

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AccountType", title = "Account type", description = "Account type")
data class AccountTypeValueObject (

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(name = "value", description = "Currency code")
   var value: String? = null,

   @field:Size(min = 1, max = 100)
   @field:Schema(name = "description", description = "A localized description for currency")
   var description: String? = null

) {

   constructor(currencyType: AccountType) :
      this(
         value = currencyType.value,
         description = currencyType.description
      )

   constructor(currencyType: AccountType, localizedDescription: String) :
      this(
         value = currencyType.value,
         description = localizedDescription
      )
}
