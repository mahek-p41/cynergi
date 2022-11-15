package com.cynergisuite.middleware.accounting.general.ledger.inquiry

import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDTO
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@Introspected
@Schema(name = "GeneralLedgerInquiry", title = "Defines a general ledger inquiry", description = "Defines a general ledger inquiry")
data class GeneralLedgerInquiryDTO(

   private var _periodNetActivities: List<BigDecimal?>,

   @field:NotNull
   @field:Schema(name = "overallPeriod", description = "Overall period")
   var overallPeriod: OverallPeriodTypeDTO? = null,

   @field:Schema(name = "beginningBalance", description = "Beginning balance")
   var beginningBalance: BigDecimal? = null,

   @field:Schema(name = "closingBalance", description = "Closing balance")
   var closingBalance: BigDecimal? = null,

   private var _priorPeriodNetActivities: List<BigDecimal?>,

   @field:NotNull
   @field:Schema(name = "priorOverallPeriod", description = "Prior overall period")
   var priorOverallPeriod: OverallPeriodTypeDTO? = null,

   @field:Schema(name = "priorBeginningBalance", description = "Prior beginning balance")
   var priorBeginningBalance: BigDecimal? = null,

   @field:Schema(name = "priorClosingBalance", description = "Prior closing balance")
   var priorClosingBalance: BigDecimal? = null,

   ) {
   constructor(entity: GeneralLedgerInquiryEntity) :
      this(
         overallPeriod = OverallPeriodTypeDTO(entity.overallPeriod),
         _periodNetActivities = entity.netActivityPeriod,
         beginningBalance = entity.beginningBalance,
         closingBalance = entity.closingBalance,
         priorOverallPeriod = entity.priorOverallPeriod?.let { OverallPeriodTypeDTO(it) },
         _priorPeriodNetActivities = entity.priorNetActivityPeriod,
         priorBeginningBalance = entity.priorBeginningBalance,
         priorClosingBalance = entity.priorClosingBalance,
      )

   private val _cumulativeNetActivities: List<BigDecimal?> = _periodNetActivities.runningFold(beginningBalance) { sum, period -> sum!!.add(period) }.drop(1)
   private val _priorCumulativeNetActivities: List<BigDecimal?> = _priorPeriodNetActivities.runningFold(priorBeginningBalance) { sum, period -> sum!!.add(period) }.drop(1)

   @field:Schema(name = "totalBalance", description = "Total Balance")
   val totalBalance = _cumulativeNetActivities.last()

   @field:Schema(name = "periodNetActivities", description = "Period Net Activities")
   val periodNetActivities: List<GLInquiryNetActivityDTO?> = _periodNetActivities.map { GLInquiryNetActivityDTO(it, totalBalance) }

   @field:Schema(name = "cumulativeNetActivities", description = "Cumulative Net Activities")
   val cumulativeNetActivities: List<GLInquiryNetActivityDTO?> = _cumulativeNetActivities.map { GLInquiryNetActivityDTO(it, totalBalance) }

   @field:Schema(name = "priorTotalBalance", description = "Total Prior Balance")
   val priorTotalBalance = _priorCumulativeNetActivities.last()

   @field:Schema(name = "priorPeriodNetActivities", description = "Prior Period Net Activities")
   val priorPeriodNetActivities: List<GLInquiryNetActivityDTO?> = _priorPeriodNetActivities.map { GLInquiryNetActivityDTO(it, priorTotalBalance) }

   @field:Schema(name = "priorCumulativeNetActivities", description = "Prior Cumulative Net Activities")
   val priorCumulativeNetActivities: List<GLInquiryNetActivityDTO?> = _priorCumulativeNetActivities.map { GLInquiryNetActivityDTO(it, priorTotalBalance) }
}
