package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.Store

data class BankEntity(
   val id: Long? = null,
   val number: Long,
   val company: Company,
   val name: String,
   val generalLedgerProfitCenter: Store,
   val generalLedgerAccount: AccountEntity
) : Identifiable {

   constructor(bankDTO: BankDTO, company: Company, store: Store, account: AccountEntity) :
      this(
         id = bankDTO.id,
         number = bankDTO.number!!,
         company = company,
         name = bankDTO.name!!,
         generalLedgerProfitCenter = store,
         generalLedgerAccount = account
      )

   override fun myId(): Long? = id
}
