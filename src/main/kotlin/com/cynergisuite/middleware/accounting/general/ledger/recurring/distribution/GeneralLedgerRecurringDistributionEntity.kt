package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringEntity
import java.math.BigDecimal

data class GeneralLedgerRecurringDistributionEntity(
   val id: Long? = null,
   val generalLedgerRecurring: GeneralLedgerRecurringEntity,
   val generalLedgerDistributionAccount: AccountEntity,
   val generalLedgerDistributionProfitCenter: Identifiable,
   val generalLedgerDistributionAmount: BigDecimal

) : Identifiable {

   constructor(
      dto: GeneralLedgerRecurringDistributionDTO,
      generalLedgerRecurring: GeneralLedgerRecurringEntity,
      generalLedgerDistributionAccount: AccountEntity
   ) :
      this(
         id = dto.id,
         generalLedgerRecurring = generalLedgerRecurring,
         generalLedgerDistributionAccount = generalLedgerDistributionAccount,
         generalLedgerDistributionProfitCenter = dto.generalLedgerDistributionProfitCenter!!,
         generalLedgerDistributionAmount = dto.generalLedgerDistributionAmount!!
      )

   override fun myId(): Long? = id
}
