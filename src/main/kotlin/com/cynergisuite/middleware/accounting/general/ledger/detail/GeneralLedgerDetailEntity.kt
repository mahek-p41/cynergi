package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.store.Store
import java.math.BigDecimal
import java.time.LocalDate

data class GeneralLedgerDetailEntity(
   val id: Long? = null,
   val account: AccountEntity,
   val profitCenter: Store,
   val date: LocalDate,
   val source: GeneralLedgerSourceCodeEntity,
   val amount: BigDecimal,
   val message: String? = null,
   val employeeNumberId: Int? = null,
   val journalEntryNumber: Int? = null

) : Identifiable {

   constructor(
      dto: GeneralLedgerDetailDTO,
      account: AccountEntity,
      profitCenter: Store,
      source: GeneralLedgerSourceCodeEntity
   ) :
      this(
         id = dto.id,
         account = account,
         date = dto.date!!,
         profitCenter = profitCenter,
         source = source,
         amount = dto.amount!!,
         message = dto.message,
         employeeNumberId = dto.employeeNumberId,
         journalEntryNumber = dto.journalEntryNumber
      )

   override fun myId(): Long? = id
}
