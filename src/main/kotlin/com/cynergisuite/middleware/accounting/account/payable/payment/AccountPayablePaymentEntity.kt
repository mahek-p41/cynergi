package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.bank.BankEntity
import com.cynergisuite.middleware.vendor.VendorEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class AccountPayablePaymentEntity(
   val id: UUID? = null,
   val bank: BankEntity,
   val vendor: VendorEntity,
   val status: AccountPayablePaymentStatusType,
   val type: AccountPayablePaymentTypeType,
   val paymentDate: LocalDate,
   val dateCleared: LocalDate?,
   val dateVoided: LocalDate?,
   val paymentNumber: String,
   val amount: BigDecimal,
   val paymentDetails: MutableSet<AccountPayablePaymentDetailEntity>? = LinkedHashSet()
) : Identifiable {

   constructor(
      dto: AccountPayablePaymentDTO,
      bank: BankEntity,
      vendor: VendorEntity,
      status: AccountPayablePaymentStatusType,
      type: AccountPayablePaymentTypeType,
      paymentDetails: MutableSet<AccountPayablePaymentDetailEntity>? = null
   ) :
      this(
         id = dto.id,
         bank = bank,
         vendor = vendor,
         status = status,
         type = type,
         paymentDate = dto.paymentDate!!,
         dateCleared = dto.dateCleared,
         dateVoided = dto.dateVoided,
         paymentNumber = dto.paymentNumber!!,
         amount = dto.amount!!,
         paymentDetails = paymentDetails
      )

   override fun myId(): UUID? = id
}
