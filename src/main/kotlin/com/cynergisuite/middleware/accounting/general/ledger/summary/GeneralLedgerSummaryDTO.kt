package com.cynergisuite.middleware.accounting.general.ledger.summary

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodTypeDTO
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Schema(name = "GeneralLedgerSummary", title = "Defines a general ledger summary", description = "Defines a general ledger summary")
data class GeneralLedgerSummaryDTO(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Schema(name = "account", description = "Account ID")
   var account: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(name = "profitCenter", description = "Profit center ID")
   var profitCenter: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(name = "overallPeriod", description = "Overall period")
   var overallPeriod: OverallPeriodTypeDTO? = null,

   @field:Schema(name = "netActivityPeriod1", description = "Net activity period 1", required = false)
   var netActivityPeriod1: BigDecimal? = null,

   @field:Schema(name = "netActivityPeriod2", description = "Net activity period 2", required = false)
   var netActivityPeriod2: BigDecimal? = null,

   @field:Schema(name = "netActivityPeriod3", description = "Net activity period 3", required = false)
   var netActivityPeriod3: BigDecimal? = null,

   @field:Schema(name = "netActivityPeriod4", description = "Net activity period 4", required = false)
   var netActivityPeriod4: BigDecimal? = null,

   @field:Schema(name = "netActivityPeriod5", description = "Net activity period 5", required = false)
   var netActivityPeriod5: BigDecimal? = null,

   @field:Schema(name = "netActivityPeriod6", description = "Net activity period 6", required = false)
   var netActivityPeriod6: BigDecimal? = null,

   @field:Schema(name = "netActivityPeriod7", description = "Net activity period 7", required = false)
   var netActivityPeriod7: BigDecimal? = null,

   @field:Schema(name = "netActivityPeriod8", description = "Net activity period 8", required = false)
   var netActivityPeriod8: BigDecimal? = null,

   @field:Schema(name = "netActivityPeriod9", description = "Net activity period 9", required = false)
   var netActivityPeriod9: BigDecimal? = null,

   @field:Schema(name = "netActivityPeriod10", description = "Net activity period 10", required = false)
   var netActivityPeriod10: BigDecimal? = null,

   @field:Schema(name = "netActivityPeriod11", description = "Net activity period 11", required = false)
   var netActivityPeriod11: BigDecimal? = null,

   @field:Schema(name = "netActivityPeriod12", description = "Net activity period 12", required = false)
   var netActivityPeriod12: BigDecimal? = null,

   @field:Schema(name = "beginningBalance", description = "Beginning balance", required = false)
   var beginningBalance: BigDecimal? = null,

   @field:Schema(name = "closingBalance", description = "Closing balance", required = false)
   var closingBalance: BigDecimal? = null

) : Identifiable {
   constructor(entity: GeneralLedgerSummaryEntity) :
      this(
         id = entity.id,
         account = SimpleIdentifiableDTO(entity.account.id),
         profitCenter = SimpleIdentifiableDTO(entity.profitCenter.myId()),
         overallPeriod = OverallPeriodTypeDTO(entity.overallPeriod),
         netActivityPeriod1 = entity.netActivityPeriod1,
         netActivityPeriod2 = entity.netActivityPeriod2,
         netActivityPeriod3 = entity.netActivityPeriod3,
         netActivityPeriod4 = entity.netActivityPeriod4,
         netActivityPeriod5 = entity.netActivityPeriod5,
         netActivityPeriod6 = entity.netActivityPeriod6,
         netActivityPeriod7 = entity.netActivityPeriod7,
         netActivityPeriod8 = entity.netActivityPeriod8,
         netActivityPeriod9 = entity.netActivityPeriod9,
         netActivityPeriod10 = entity.netActivityPeriod10,
         netActivityPeriod11 = entity.netActivityPeriod11,
         netActivityPeriod12 = entity.netActivityPeriod12,
         beginningBalance = entity.beginningBalance,
         closingBalance = entity.closingBalance
      )

   override fun myId(): Long? = id
}
