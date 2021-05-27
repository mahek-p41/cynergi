package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import java.time.LocalDate

data class GeneralLedgerRecurringEntity(
   val id: Long? = null,
   val source: GeneralLedgerSourceCodeEntity,
   val type: GeneralLedgerRecurringType,
   val reverseIndicator: Boolean,
   val message: String? = null,
   val beginDate: LocalDate,
   val endDate: LocalDate? = null,
   val lastTransferDate: LocalDate? = null

) : Identifiable {

   constructor(
      dto: GeneralLedgerRecurringDTO,
      source: GeneralLedgerSourceCodeEntity,
      type: GeneralLedgerRecurringType
   ) :
      this(
         id = dto.id,
         source = source,
         type = type,
         reverseIndicator = dto.reverseIndicator!!,
         message = dto.message,
         beginDate = dto.beginDate!!,
         endDate = dto.endDate,
         lastTransferDate = dto.lastTransferDate
      )

   override fun myId(): Long? = id
}
