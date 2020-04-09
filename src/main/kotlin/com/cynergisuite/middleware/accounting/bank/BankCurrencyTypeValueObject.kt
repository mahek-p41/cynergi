package com.cynergisuite.middleware.accounting.bank

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "BankCurrencyType", title = "Bank currency", description = "Currencies that the banks support")
data class BankCurrencyTypeValueObject (

   @field:NotNull
   @field:Size(min = 3, max = 15)
   @field:Schema(description = "Currency code")
   var value: String? = null,

   @field:Nullable
   @field:Size(min = 3, max = 50)
   @field:Schema(description = "A localized description for currency")
   var description: String? = null

) {

   constructor(entity: BankCurrencyType) :
      this(
         entity = entity,
         localizedDescription = entity.description
      )

   constructor(entity: BankCurrencyType, localizedDescription: String) :
      this(
         value = entity.value,
         description = localizedDescription
      )
}
