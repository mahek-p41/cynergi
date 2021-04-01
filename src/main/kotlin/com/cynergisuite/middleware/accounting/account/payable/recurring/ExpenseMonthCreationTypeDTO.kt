package com.cynergisuite.middleware.accounting.account.payable.recurring

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "ExpenseMonthCreationType", title = "Expense month creation type", description = "Expense month creation type")
data class ExpenseMonthCreationTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Expense month creation type")
   var value: String,

   @field:Nullable
   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for expense month creation")
   var description: String? = null

) {

   constructor(type: ExpenseMonthCreationType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: ExpenseMonthCreationType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
