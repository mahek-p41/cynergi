package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

@Introspected
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "AccountPayablePaymentDistribution", title = "Account Payable Payment Distribution", description = "Account Payable Payment Distribution")
data class AccountPayablePaymentDistributionDTO(

   @field:Schema(description = "Account Payable Payment Distribution id")
   var id: UUID? = null,

   @field:Schema(description = "Account Payable Payment id")
   var paymentId: UUID,

   @field:Schema(description = "Distribution Account id")
   var distributionAccount: UUID,

   @field:Schema(description = "Distribution Profit Center")
   var distributionProfitCenter: Long,

   @field:Schema(description = "Distribution Amount")
   var distributionAmount: BigDecimal

) : Identifiable {
   constructor(entity: AccountPayablePaymentDistributionEntity) :
      this(
         entity.id,
         entity.paymentId,
         entity.distributionAccount,
         entity.distributionProfitCenter,
         entity.distributionAmount
      )

   override fun myId(): UUID? = id
}
