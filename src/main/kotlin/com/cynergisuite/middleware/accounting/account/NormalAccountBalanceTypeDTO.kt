package com.cynergisuite.middleware.accounting.account

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "NormalAccountBalanceType", title = "Normal account balance type", description = "Normal account balance")
data class NormalAccountBalanceTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(name = "value", description = "Normal account balance")
   var value: String? = null,

   @field:Size(min = 1, max = 100)
   @field:Schema(name = "description", description = "A localized description for normal account balance")
   var description: String? = null

) {
   constructor(currencyType: NormalAccountBalanceType) :
      this(
         value = currencyType.value,
         description = currencyType.description
      )

   constructor(currencyType: NormalAccountBalanceType, localizedDescription: String) :
      this(
         value = currencyType.value,
         description = localizedDescription
      )
}
