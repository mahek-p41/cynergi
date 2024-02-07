package com.cynergisuite.middleware.accounting.account.payable.invoice

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
   val bank: UUID,
   val externalPaymentTypeId: AccountPayablePaymentTypeType?,
   val externalPaymentNumber: String?,
   val externalPaymentDate: LocalDate?,
   val selectedForProcessing: Boolean,
   val paymentProcessed: Boolean,

   )
