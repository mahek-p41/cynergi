package com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalEntity
import com.cynergisuite.middleware.store.Store
import java.math.BigDecimal
import java.util.UUID

data class GeneralLedgerReversalDistributionEntity(
   val id: UUID? = null,
   var generalLedgerReversal: GeneralLedgerReversalEntity,
   val generalLedgerReversalDistributionAccount: AccountEntity,
   val generalLedgerReversalDistributionProfitCenter: Store,
   val generalLedgerReversalDistributionAmount: BigDecimal

) : Identifiable {

   constructor(
      id: UUID?,
      dto: GeneralLedgerReversalDistributionDTO,
      generalLedgerReversal: GeneralLedgerReversalEntity,
      generalLedgerReversalDistributionAccount: AccountEntity,
      generalLedgerReversalDistributionProfitCenter: Store
   ) :
      this(
         id = id,
         generalLedgerReversal = generalLedgerReversal,
         generalLedgerReversalDistributionAccount = generalLedgerReversalDistributionAccount,
         generalLedgerReversalDistributionProfitCenter = generalLedgerReversalDistributionProfitCenter,
         generalLedgerReversalDistributionAmount = dto.generalLedgerReversalDistributionAmount!!
      )

   override fun myId(): UUID? = id
}
