package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerRecurringType", description = "General ledger recurring type")
data class GeneralLedgerRecurringTypeDTO(

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "General ledger recurring type")
   var value: String,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for general ledger recurring type")
   var description: String? = null

) {

   constructor(type: GeneralLedgerRecurringType) :
      this(
         value = type.value,
         description = type.description
      )

   constructor(type: GeneralLedgerRecurringType, localizedDescription: String) :
      this(
         value = type.value,
         description = localizedDescription
      )
}
