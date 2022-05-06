package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringEntity
import com.cynergisuite.middleware.store.Store
import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.util.UUID

@Introspected
data class GeneralLedgerRecurringDistributionEntity(
   val id: UUID? = null,
   var generalLedgerRecurring: GeneralLedgerRecurringEntity,
   val generalLedgerDistributionAccount: AccountEntity,
   val generalLedgerDistributionProfitCenter: Store,
   val generalLedgerDistributionAmount: BigDecimal

) : Identifiable {

   constructor(
      id: UUID?,
      dto: GeneralLedgerRecurringDistributionDTO,
      generalLedgerRecurring: GeneralLedgerRecurringEntity,
      generalLedgerDistributionAccount: AccountEntity,
      generalLedgerDistributionProfitCenter: Store
   ) :
      this(
         id = id,
         generalLedgerRecurring = generalLedgerRecurring,
         generalLedgerDistributionAccount = generalLedgerDistributionAccount,
         generalLedgerDistributionProfitCenter = generalLedgerDistributionProfitCenter,
         generalLedgerDistributionAmount = dto.generalLedgerDistributionAmount!!
      )

   override fun myId(): UUID? = id
}
