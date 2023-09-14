package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.store.Store
import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Introspected
data class GeneralLedgerJournalEntity(
   val id: UUID? = null,
   val account: AccountEntity,
   val profitCenter: Store,
   val date: LocalDate,
   val source: GeneralLedgerSourceCodeEntity,
   val amount: BigDecimal,
   val message: String?
) : Identifiable {

   constructor(id: UUID?, dto: GeneralLedgerJournalDTO, account: AccountEntity, profitCenter: Store, source: GeneralLedgerSourceCodeEntity) :
      this(
         id = id,
         account = account,
         profitCenter = profitCenter,
         date = dto.date!!,
         source = source,
         amount = dto.amount!!,
         message = dto.message
      )

   override fun myId(): UUID? = id
}
