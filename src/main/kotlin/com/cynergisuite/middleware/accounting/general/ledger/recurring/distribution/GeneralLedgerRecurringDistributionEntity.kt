package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.store.Store
import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.util.UUID

@Introspected
data class GeneralLedgerRecurringDistributionEntity(
   val id: UUID? = null,
   var generalLedgerRecurringId: UUID?,
   val generalLedgerDistributionAccount: AccountEntity,
   val generalLedgerDistributionProfitCenter: Store,
   val generalLedgerDistributionAmount: BigDecimal

) : Identifiable {

   constructor(
      id: UUID?,
      dto: GeneralLedgerRecurringDistributionDTO,
      generalLedgerDistributionAccount: AccountEntity,
      generalLedgerDistributionProfitCenter: Store
   ) :
      this(
         id = id,
         generalLedgerRecurringId = dto.generalLedgerRecurring!!.id,
         generalLedgerDistributionAccount = generalLedgerDistributionAccount,
         generalLedgerDistributionProfitCenter = generalLedgerDistributionProfitCenter,
         generalLedgerDistributionAmount = dto.generalLedgerDistributionAmount!!
      )

   override fun myId(): UUID? = id
}
