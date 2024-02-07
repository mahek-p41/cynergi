package com.cynergisuite.middleware.accounting.account.payable.invoice

import java.math.BigDecimal
import java.util.UUID

data class AccountPayableInvoiceDistributionEntity(
   val id: UUID? = null,
   val invoiceId: UUID,
   val accountId: UUID,
   val profitCenter: Long,
   val amount: BigDecimal
)
