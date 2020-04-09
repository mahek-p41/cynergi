package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Identifiable
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
   val number: Int,
   val name: String,
   val generalLedgerProfitCenter: Store,
   val accountNumber: Int,
   val currency: BankCurrencyType
) : Identifiable {
   constructor(bankDTO: BankDTO) :
      this(
         id = bankDTO.id,
         company = bankDTO.company,
         address = AddressEntity(bankDTO.address),
         number = bankDTO.number,
         name = bankDTO.name,
         generalLedgerProfitCenter = bankDTO.generalLedgerProfitCenter,
         accountNumber = bankDTO.accountNumber,
         currency = bankDTO.currency
      )

   constructor(bankDTO: BankDTO, company: Company) :
      this(
         id = bankDTO.id,
         company = company,
         address = AddressEntity(bankDTO.address),
         number = bankDTO.number,
         name = bankDTO.name,
         generalLedgerProfitCenter = bankDTO.generalLedgerProfitCenter,
         accountNumber = bankDTO.accountNumber,
         currency = bankDTO.currency
      )

   override fun myId(): Long? = id
}
