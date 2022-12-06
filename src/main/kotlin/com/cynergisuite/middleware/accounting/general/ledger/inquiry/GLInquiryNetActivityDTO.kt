package com.cynergisuite.middleware.accounting.general.ledger.inquiry

import com.cynergisuite.extensions.equalTo
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.math.RoundingMode

@Introspected
@Schema(name = "GLInquiryNetActivityDTO", title = "Defines a general ledger inquiry net activity", description = "Defines a general ledger inquiry net activity")
data class GLInquiryNetActivityDTO(

   @field:Schema(name = "netActivity", description = "Net activity", required = false)
   val netActivity: BigDecimal? = null,

   @field:Schema(name = "balance", description = "Balance", required = false)
   private val totalBalance: BigDecimal? = null,

   ) {
   val percent: BigDecimal?
      get() = if (!totalBalance!!.equalTo(BigDecimal.ZERO)) netActivity!!.divide(totalBalance, 4, RoundingMode.HALF_UP) else BigDecimal.ZERO
}
