package com.cynergisuite.middleware.accounting.general.ledger.reversal

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Schema(name = "GeneralLedgerReversal", title = "Defines a general ledger reversal", description = "Defines a general ledger reversal")
data class GeneralLedgerReversalDTO(

   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(name = "source", description = "General ledger source code")
   var source: GeneralLedgerSourceCodeDTO? = null,

   @field:NotNull
   @field:Schema(name = "date", description = "Date")
   var date: LocalDate? = null,

   @field:NotNull
   @field:Schema(name = "reversalDate", description = "Reversal date")
   var reversalDate: LocalDate? = null,

   @field:Schema(name = "comment", required = false, description = "Comment")
   var comment: String? = null,

   @field:Positive
   @field:NotNull
   @field:Schema(name = "entryMonth", description = "Entry month")
   var entryMonth: Int? = null,

   @field:Positive
   @field:NotNull
   @field:Schema(name = "entryNumber", description = "Entry number")
   var entryNumber: Int? = null

) : Identifiable {

   constructor(entity: GeneralLedgerReversalEntity) :
      this(
         id = entity.id,
         source = GeneralLedgerSourceCodeDTO(entity.source),
         date = entity.date,
         reversalDate = entity.reversalDate,
         comment = entity.comment,
         entryMonth = entity.entryMonth,
         entryNumber = entity.entryNumber
      )

   override fun myId(): UUID? = id
}
