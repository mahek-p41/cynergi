package com.cynergisuite.middleware.accounting.bank.reconciliation

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.bank.BankEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationType
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.Store
import java.math.BigDecimal
import java.time.LocalDate

data class BankReconciliationEntity(
   val id: Long? = null,
   val number: Long? = null,
   val company: Company,
   val name: String,
   var bank: BankEntity,
   var type: BankReconciliationType,
   var date: LocalDate,
   var clearedDate: LocalDate?,
   var amount: BigDecimal,
   var description: String,
   var document: Int?
) : Identifiable {

   constructor(bankDTO: BankReconciliationDTO, company: Company, store: Store, account: AccountEntity) :
      this(
         id = null,
         number = null,
         company = company,
         dto = bankDTO,
         store = store,
         account = account
      )

   constructor(id: Long? = null, number: Long? = null, dto: BankReconciliationDTO, company: Company) :
      this(
         id = id,
         company = company,
         name = dto.name!!
      )

   override fun myId(): Long? = id
}
