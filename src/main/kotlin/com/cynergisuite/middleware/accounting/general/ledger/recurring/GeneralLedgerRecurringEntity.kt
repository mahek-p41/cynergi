package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import java.time.LocalDate
import java.util.UUID

data class GeneralLedgerRecurringEntity(
   val id: UUID? = null,
   val source: GeneralLedgerSourceCodeEntity,
   val type: GeneralLedgerRecurringType,
   val reverseIndicator: Boolean,
   val message: String? = null,
   val beginDate: LocalDate,
   val endDate: LocalDate? = null,
   var lastTransferDate: LocalDate? = null

) : Identifiable {

   constructor(
      id: UUID?,
      dto: GeneralLedgerRecurringDTO,
      source: GeneralLedgerSourceCodeEntity,
      type: GeneralLedgerRecurringType
   ) :
      this(
         id = id,
         source = source,
         type = type,
         reverseIndicator = dto.reverseIndicator!!,
         message = dto.message,
         beginDate = dto.beginDate!!,
         endDate = dto.endDate,
         lastTransferDate = dto.lastTransferDate
      )

   override fun myId(): UUID? = id
}
