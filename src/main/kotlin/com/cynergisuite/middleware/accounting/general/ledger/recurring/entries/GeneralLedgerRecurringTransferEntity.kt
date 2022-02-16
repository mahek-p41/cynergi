package com.cynergisuite.middleware.accounting.general.ledger.recurring.entries

import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailEntity
import java.time.LocalDate
import java.util.*

data class GeneralLedgerRecurringTransferEntity(
   val generalLedgerRecurringId: UUID,
   val lastTransferDate: LocalDate? = null,
   val generalLedgerDetail: GeneralLedgerDetailEntity
) {}
