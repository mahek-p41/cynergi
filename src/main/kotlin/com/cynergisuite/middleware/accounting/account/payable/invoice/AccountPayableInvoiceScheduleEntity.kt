package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentTypeType
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class AccountPayableInvoiceScheduleEntity (
   val id: UUID? = null,
   val invoiceId: UUID,
   val companyId: UUID,
   val scheduleDate: LocalDate,
   val paymentSequenceNumber: Int,
   val amountToPay: BigDecimal,
   val bank: UUID? = null,
   val externalPaymentTypeId: AccountPayablePaymentTypeType? = null,
   val externalPaymentNumber: String? = null,
   val externalPaymentDate: LocalDate? = null,
   val selectedForProcessing: Boolean,
   val paymentProcessed: Boolean,

) : Identifiable {

   constructor(
      dto: AccountPayableInvoiceScheduleDTO,
      type: AccountPayablePaymentTypeType?
      ) :
      this(
         id = dto.id,
         invoiceId = dto.invoiceId!!,
         companyId = dto.companyId!!,
         scheduleDate = dto.scheduleDate!!,
         paymentSequenceNumber = dto.paymentSequenceNumber!!,
         amountToPay = dto.amountToPay!!,
         bank = dto.bank,
         externalPaymentTypeId = type,
         externalPaymentNumber = dto.externalPaymentNumber,
         externalPaymentDate = dto.externalPaymentDate,
         selectedForProcessing = dto.selectedForProcessing!!,
         paymentProcessed = dto.paymentProcessed!!
      )

   override fun myId(): UUID? = id
}

