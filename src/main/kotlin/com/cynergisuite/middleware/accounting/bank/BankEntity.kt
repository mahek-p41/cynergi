package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.store.Store
import java.util.UUID

data class BankEntity(
   val id: UUID? = null,
   val number: Long,
   val name: String,
   val generalLedgerProfitCenter: Store,
   val generalLedgerAccount: AccountEntity
) : Identifiable {

   constructor(bankDTO: BankDTO, store: Store, account: AccountEntity) :
      this(
         id = bankDTO.id,
         number = bankDTO.number!!,
         name = bankDTO.name!!,
         generalLedgerProfitCenter = store,
         generalLedgerAccount = account
      )

   override fun myId(): UUID? = id
}
