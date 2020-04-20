package com.cynergisuite.middleware.accounting.account

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AccountType", title = "Account type", description = "Account type")
data class AccountTypeValueObject (

   @field:Positive
   @field:Schema(name = "id", description = "This is a database driven primary key value defining the id of the status")
   var id: Long? = null,

   @field:NotNull
   @field:Size(min = 3, max = 15)
   @field:Schema(description = "Currency code")
   var value: String? = null,

   @field:Nullable
   @field:Size(min = 3, max = 50)
   @field:Schema(description = "A localized description for currency")
   var description: String? = null

) {

   constructor(currencyType: AccountType) :
      this(
         id = currencyType.id,
         value = currencyType.value,
         description = currencyType.description
      )

   constructor(currencyType: AccountType, localizedDescription: String) :
      this(
         id = currencyType.id,
         value = currencyType.value,
         description = localizedDescription
      )
}
