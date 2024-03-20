package com.cynergisuite.middleware.accounting.account.payable.recurring.distribution.infrastructure

import java.math.BigDecimal
import java.util.UUID

data class AccountPayableRecurringInvoiceDistributionEntity(

   val id: UUID? = null,
   val invoiceId: UUID,
   val accountId: UUID,
   val profitCenter: Int,
   val amount: BigDecimal
)
