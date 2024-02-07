package com.cynergisuite.middleware.accounting.account.payable.payment

import java.math.BigDecimal
import java.util.UUID

data class AccountPayablePaymentDistributionEntity(
   val id: UUID? = null,
   val paymentId: UUID,
   val distributionAccount: UUID,
   val distributionProfitCenter: Long,
   val distributionAmount: BigDecimal
)
