package com.cynergisuite.middleware.accounting.account

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AccountStatusType", title = "Account status type", description = "Currencies that the banks support")
data class AccountStatusTypeValueObject(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Account status")
   var value: String? = null,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for currency")
   var description: String? = null

) {

   constructor(currencyType: AccountStatusType) :
      this(
         value = currencyType.value,
         description = currencyType.description
      )

   constructor(currencyType: AccountStatusType, localizedDescription: String) :
      this(
         value = currencyType.value,
         description = localizedDescription
      )
}
