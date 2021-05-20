package com.cynergisuite.middleware.accounting.general.ledger.reversal

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import java.time.LocalDate
import java.util.UUID

data class GeneralLedgerReversalEntity(
   val id: UUID? = null,
   val source: GeneralLedgerSourceCodeEntity,
   val date: LocalDate,
   val reversalDate: LocalDate,
   val comment: String?,
   val entryMonth: Int,
   val entryNumber: Int
) : Identifiable {

   constructor(id: UUID?, dto: GeneralLedgerReversalDTO, source: GeneralLedgerSourceCodeEntity) :
      this(
         id = id,
         source = source,
         date = dto.date!!,
         reversalDate = dto.reversalDate!!,
         comment = dto.comment,
         entryMonth = dto.entryMonth!!,
         entryNumber = dto.entryNumber!!
      )

   override fun myId(): UUID? = id
}
