package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.store.Store
import java.math.BigDecimal
import java.util.UUID

data class AccountPayableDistributionDetailEntity(
   val id: UUID? = null,
   val profitCenter: Store,
   val account: AccountEntity,
   val percent: BigDecimal,
   val distributionTemplate: AccountPayableDistributionTemplateEntity
) : Identifiable {

   constructor(
      dto: AccountPayableDistributionDetailDTO,
      profitCenter: Store,
      account: AccountEntity,
      distributionTemplate: AccountPayableDistributionTemplateEntity
   ) :
      this(
         id = dto.id,
         profitCenter = profitCenter,
         account = account,
         percent = dto.percent!!,
         distributionTemplate = distributionTemplate
      )

   override fun myId(): UUID? = id
}
