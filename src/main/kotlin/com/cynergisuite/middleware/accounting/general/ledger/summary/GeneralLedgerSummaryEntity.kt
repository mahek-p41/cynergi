package com.cynergisuite.middleware.accounting.general.ledger.summary

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodType
import com.cynergisuite.middleware.store.Store
import java.math.BigDecimal
import java.util.UUID

data class GeneralLedgerSummaryEntity(
   val id: UUID? = null,
   val account: AccountEntity,
   val profitCenter: Store,
   val overallPeriod: OverallPeriodType,
   val netActivityPeriod1: BigDecimal?,
   val netActivityPeriod2: BigDecimal?,
   val netActivityPeriod3: BigDecimal?,
   val netActivityPeriod4: BigDecimal?,
   val netActivityPeriod5: BigDecimal?,
   val netActivityPeriod6: BigDecimal?,
   val netActivityPeriod7: BigDecimal?,
   val netActivityPeriod8: BigDecimal?,
   val netActivityPeriod9: BigDecimal?,
   val netActivityPeriod10: BigDecimal?,
   val netActivityPeriod11: BigDecimal?,
   val netActivityPeriod12: BigDecimal?,
   val beginningBalance: BigDecimal?,
   val closingBalance: BigDecimal?
) : Identifiable {

   constructor(dto: GeneralLedgerSummaryDTO, account: AccountEntity, profitCenter: Store, overallPeriod: OverallPeriodType) :
      this(
         id = dto.id,
         account = account,
         profitCenter = profitCenter,
         overallPeriod = overallPeriod,
         netActivityPeriod1 = dto.netActivityPeriod1,
         netActivityPeriod2 = dto.netActivityPeriod2,
         netActivityPeriod3 = dto.netActivityPeriod3,
         netActivityPeriod4 = dto.netActivityPeriod4,
         netActivityPeriod5 = dto.netActivityPeriod5,
         netActivityPeriod6 = dto.netActivityPeriod6,
         netActivityPeriod7 = dto.netActivityPeriod7,
         netActivityPeriod8 = dto.netActivityPeriod8,
         netActivityPeriod9 = dto.netActivityPeriod9,
         netActivityPeriod10 = dto.netActivityPeriod10,
         netActivityPeriod11 = dto.netActivityPeriod11,
         netActivityPeriod12 = dto.netActivityPeriod12,
         beginningBalance = dto.beginningBalance,
         closingBalance = dto.closingBalance
      )

   override fun myId(): UUID? = id
}
