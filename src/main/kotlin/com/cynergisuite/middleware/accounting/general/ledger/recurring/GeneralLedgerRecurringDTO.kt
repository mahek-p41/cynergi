package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerRecurring", title = "General Ledger Recurring", description = "General ledger recurring entity")
data class GeneralLedgerRecurringDTO(

   @field:Schema(description = "General ledger recurring id")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "General ledger recurring reverse indicator")
   var reverseIndicator: Boolean? = null,

   @field:Schema(description = "General ledger recurring message", required = false)
   var message: String? = null,

   @field:NotNull
   @field:Schema(description = "General ledger recurring source")
   var source: GeneralLedgerSourceCodeDTO? = null,

   @field:NotNull
   @field:Schema(description = "General ledger recurring type")
   var type: GeneralLedgerRecurringTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "General ledger recurring begin date")
   var beginDate: LocalDate? = null,

   @field:Schema(description = "General ledger recurring end date", required = false)
   var endDate: LocalDate? = null,

   @field:Schema(description = "General ledger recurring last transfer date", required = false)
   var lastTransferDate: LocalDate? = null

) : Identifiable {
   constructor(
      entity: GeneralLedgerRecurringEntity
   ) :
      this(
         id = entity.id,
         reverseIndicator = entity.reverseIndicator,
         message = entity.message,
         source = GeneralLedgerSourceCodeDTO(entity.source),
         type = GeneralLedgerRecurringTypeDTO(entity.type),
         beginDate = entity.beginDate,
         endDate = entity.endDate,
         lastTransferDate = entity.lastTransferDate
      )

   override fun myId(): UUID? = id
}
