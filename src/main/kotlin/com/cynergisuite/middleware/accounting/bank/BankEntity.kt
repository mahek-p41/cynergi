package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.Store
import java.time.OffsetDateTime

data class BankEntity(
   val id: Long? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val company: Company,
   val address: AddressEntity,
   val number: Int? = null,
   val name: String,
   val generalLedgerProfitCenter: Store,
   val generalLedgerAccount: AccountEntity,
   val accountNumber: Int,
   val currency: BankCurrencyType
) : Identifiable {

   constructor(bankDTO: BankDTO, company: Company, store: Store, account: AccountEntity, currencyType: BankCurrencyType) :
      this(
         id = bankDTO.id,
         company = company,
         address = AddressEntity(bankDTO.address),
         number = bankDTO.number,
         name = bankDTO.name,
         generalLedgerProfitCenter = store,
         generalLedgerAccount = account,
         accountNumber = bankDTO.accountNumber,
         currency = currencyType
      )

   override fun myId(): Long? = id
}
