package com.cynergisuite.middleware.accounting.bank.reconciliation

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.bank.BankEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationType
import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Introspected
data class BankReconciliationEntity(
   val id: UUID? = null,
   var bank: BankEntity,
   var type: BankReconciliationType,
   var date: LocalDate,
   var clearedDate: LocalDate?,
   var amount: BigDecimal,
   var description: String,
   var document: String?
) : Identifiable {

   constructor(dto: BankReconciliationDTO, bank: BankEntity, type: BankReconciliationType) :
      this(
         id = null,
         dto = dto,
         bank = bank,
         type = type
      )

   constructor(id: UUID? = null, dto: BankReconciliationDTO, bank: BankEntity, type: BankReconciliationType) :
      this(
         id = id,
         bank = bank,
         type = type,
         date = dto.date!!,
         clearedDate = dto.clearedDate,
         amount = dto.amount!!,
         description = dto.description!!,
         document = dto.document
      )

   override fun myId(): UUID? = id
}
