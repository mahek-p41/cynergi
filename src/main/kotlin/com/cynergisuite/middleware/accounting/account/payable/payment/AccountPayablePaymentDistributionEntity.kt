package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.Identifiable
import java.math.BigDecimal
import java.util.UUID

data class AccountPayablePaymentDistributionEntity(
   val id: UUID? = null,
   val paymentId: UUID,
   val distributionAccount: UUID,
   val distributionProfitCenter: Long,
   val distributionAmount: BigDecimal
) : Identifiable {
   constructor(
      dto: AccountPayablePaymentDistributionDTO
   ) :
      this(
         dto.id,
         dto.paymentId,
         dto.distributionAccount,
         dto.distributionProfitCenter,
         dto.distributionAmount
      )

   override fun myId(): UUID? = id
}

