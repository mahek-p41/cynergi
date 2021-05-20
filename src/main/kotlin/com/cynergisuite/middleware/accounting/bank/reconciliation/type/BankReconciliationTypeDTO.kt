package com.cynergisuite.middleware.accounting.bank.reconciliation.type

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "BankReconciliationType", title = "Bank reconciliation type", description = "Bank reconciliation type")
data class BankReconciliationTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Bank reconciliation type")
   var value: String,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for bank reconciliation type")
   var description: String? = null

) {

   constructor(type: BankReconciliationType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: BankReconciliationType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
