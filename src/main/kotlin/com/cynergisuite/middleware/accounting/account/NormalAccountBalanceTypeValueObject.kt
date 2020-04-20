package com.cynergisuite.middleware.accounting.account

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "NormalAccountBalanceType", title = "Normal account balance type", description = "Normal account balance")
data class NormalAccountBalanceTypeValueObject (

   @field:Positive
   @field:Schema(name = "id", description = "This is a database driven primary key value defining the id of the status")
   var id: Long? = null,

   @field:NotNull
   @field:Size(min = 3, max = 15)
   @field:Schema(description = "Normal account balance")
   var value: String? = null,

   @field:Nullable
   @field:Size(min = 3, max = 50)
   @field:Schema(description = "A localized description for normal account balance")
   var description: String? = null

) {
   constructor(currencyType: NormalAccountBalanceType) :
      this(
         id = currencyType.id,
         value = currencyType.value,
         description = currencyType.description
      )

   constructor(currencyType: NormalAccountBalanceType, localizedDescription: String) :
      this(
         id = currencyType.id,
         value = currencyType.value,
         description = localizedDescription
      )
}
