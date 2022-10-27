package com.cynergisuite.middleware.accounting.general.ledger.inquiry

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodType
import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.util.UUID

@Introspected
data class GeneralLedgerInquiryEntity(
   val id: UUID? = null,
   val overallPeriod: OverallPeriodType,
   val netActivityPeriod: List<BigDecimal?>,
   val beginningBalance: BigDecimal,
   val closingBalance: BigDecimal,
   val priorNetActivityPeriod: List<BigDecimal?>,
   val priorOverallPeriod: OverallPeriodType,
   val priorBeginningBalance: BigDecimal,
   val priorClosingBalance: BigDecimal,
) : Identifiable {

   override fun myId(): UUID? = id

}
