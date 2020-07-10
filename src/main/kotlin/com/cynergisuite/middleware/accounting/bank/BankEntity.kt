package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.Store

data class BankEntity(
   val id: Long? = null,
   val company: Company,
   val name: String,
   val generalLedgerProfitCenter: Store,
   val generalLedgerAccount: AccountEntity
) : Identifiable {

   constructor(bankDTO: BankDTO, company: Company, store: Store, account: AccountEntity) :
      this(
         id = null,
         bankDTO = bankDTO,
         company = company,
         store = store,
         account = account
      )

   constructor(id: Long? = null, bankDTO: BankDTO, company: Company, store: Store, account: AccountEntity) :
      this(
         id = id,
         company = company,
         name = bankDTO.name!!,
         generalLedgerProfitCenter = store,
         generalLedgerAccount = account
      )

   override fun myId(): Long? = id
}
