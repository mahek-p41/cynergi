package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.Identifiable
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Schema(name = "GeneralLedgerSourceCode", title = "Defines a general ledger source code", description = "Defines a general ledger source code")
data class GeneralLedgerSourceCodeDTO(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Size(min = 1, max = 3)
   @field:Schema(name = "value", minimum = "1", maximum = "3", description = "Describes the general ledger source code")
   var value: String? = null,

   @field:NotNull
   @field:Size(min = 1, max = 30)
   @field:Schema(name = "description", minimum = "1", maximum = "30", description = "Describes the general ledger source code")
   var description: String? = null

) : Identifiable {
   constructor(entity: GeneralLedgerSourceCodeEntity) :
      this(
         id = entity.id,
         value = entity.value,
         description = entity.description
      )

   override fun myId(): Long? = id
}
