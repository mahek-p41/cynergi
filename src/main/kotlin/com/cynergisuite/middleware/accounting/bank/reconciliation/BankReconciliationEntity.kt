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
   val company: Company,
   var bank: BankEntity,
   var type: BankReconciliationType,
   var date: LocalDate,
   var clearedDate: LocalDate?,
   var amount: BigDecimal,
   var description: String,
   var document: Int?
) : Identifiable {

   constructor(dto: BankReconciliationDTO, company: Company, bank: BankEntity, type: BankReconciliationType) :
      this(
         id = null,
         company = company,
         dto = dto,
         bank = bank,
         type = type
      )

   constructor(id: Long? = null, dto: BankReconciliationDTO, company: Company, bank: BankEntity, type: BankReconciliationType) :
      this(
         id = id,
         company = company,
         bank = bank,
         type = type,
         date = dto.date!!,
         clearedDate = dto.clearedDate,
         amount = dto.amount!!,
         description = dto.description!!,
         document = dto.document
      )

   override fun myId(): Long? = id
}
