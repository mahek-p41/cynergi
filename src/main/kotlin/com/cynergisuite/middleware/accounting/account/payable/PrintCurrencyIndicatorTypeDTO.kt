package com.cynergisuite.middleware.accounting.account.payable

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "PrintCurrencyIndicator", title = "Print currency indicator", description = "Print currency indicator type")
data class PrintCurrencyIndicatorTypeDTO (

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Print currency indicator type")
   var value: String,

   @field:Nullable
   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for print currency indicator")
   var description: String? = null

) {

   constructor(type: PrintCurrencyIndicatorType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: PrintCurrencyIndicatorType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
