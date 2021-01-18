package com.cynergisuite.middleware.accounting.general.ledger.reversal

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import java.time.LocalDate

data class GeneralLedgerReversalEntity(
   val id: Long? = null,
   val source: GeneralLedgerSourceCodeEntity,
   val date: LocalDate,
   val reversalDate: LocalDate,
   val comment: String?,
   val entryMonth: Int,
   val entryNumber: Int
) : Identifiable {

   constructor(dto: GeneralLedgerReversalDTO, source: GeneralLedgerSourceCodeEntity) :
      this(
         id = dto.id,
         source = source,
         date = dto.date!!,
         reversalDate = dto.reversalDate!!,
         comment = dto.comment,
         entryMonth = dto.entryMonth!!,
         entryNumber = dto.entryNumber!!
      )

   override fun myId(): Long? = id
}
