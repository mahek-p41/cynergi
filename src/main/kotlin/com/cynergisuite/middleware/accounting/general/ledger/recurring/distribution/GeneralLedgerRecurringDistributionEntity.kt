package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringEntity
import java.math.BigDecimal

data class GeneralLedgerRecurringDistributionEntity(
   val id: Long? = null,
   val generalLedgerRecurring: GeneralLedgerRecurringEntity,
   val generalLedgerDistributionAccount: Identifiable,
   val generalLedgerDistributionProfitCenter: Identifiable,
   val generalLedgerDistributionAmount: BigDecimal

) : Identifiable {

   constructor(
      dto: GeneralLedgerRecurringDistributionDTO,
      generalLedgerRecurring: GeneralLedgerRecurringEntity
   ) :
      this(
         id = dto.id,
         generalLedgerRecurring = generalLedgerRecurring,
         generalLedgerDistributionAccount = dto.generalLedgerDistributionAccount!!,
         generalLedgerDistributionProfitCenter = dto.generalLedgerDistributionProfitCenter!!,
         generalLedgerDistributionAmount = dto.generalLedgerDistributionAmount!!
      )

   override fun myId(): Long? = id
}
