package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.LegacyIdentifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringEntity
import java.math.BigDecimal
import java.util.UUID

data class GeneralLedgerRecurringDistributionEntity(
   val id: UUID? = null,
   val generalLedgerRecurring: GeneralLedgerRecurringEntity,
   val generalLedgerDistributionAccount: AccountEntity,
   val generalLedgerDistributionProfitCenter: LegacyIdentifiable,
   val generalLedgerDistributionAmount: BigDecimal

) : Identifiable {

   constructor(
      id: UUID?,
      dto: GeneralLedgerRecurringDistributionDTO,
      generalLedgerRecurring: GeneralLedgerRecurringEntity,
      generalLedgerDistributionAccount: AccountEntity
   ) :
      this(
         id = id,
         generalLedgerRecurring = generalLedgerRecurring,
         generalLedgerDistributionAccount = generalLedgerDistributionAccount,
         generalLedgerDistributionProfitCenter = dto.generalLedgerDistributionProfitCenter!!,
         generalLedgerDistributionAmount = dto.generalLedgerDistributionAmount!!
      )

   override fun myId(): UUID? = id
}
