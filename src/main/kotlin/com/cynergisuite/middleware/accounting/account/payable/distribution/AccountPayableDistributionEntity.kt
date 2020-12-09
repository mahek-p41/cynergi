package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.store.Store
import java.math.BigDecimal

data class AccountPayableDistributionEntity(
   val id: Long? = null,
   val name: String,
   val profitCenter: Store,
   val account: AccountEntity,
   val percent: BigDecimal
) : Identifiable {

   constructor(dto: AccountPayableDistributionDTO, profitCenter: Store, account: AccountEntity) :
      this(
         id = dto.id,
         name = dto.name!!,
         profitCenter = profitCenter,
         account = account,
         percent = dto.percent!!
      )

   override fun myId(): Long? = id
}
