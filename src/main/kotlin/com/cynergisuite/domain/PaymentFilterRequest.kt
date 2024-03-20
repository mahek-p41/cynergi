package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull

@Introspected
class PaymentFilterRequest(

   @NotNull
   @field:Schema(name = "bankNumber", description = "The Bank Number to filter results with")
   var bankNumber: Long,

   @NotNull
   @field:Schema(name = "PaymentNumber", description = "The Payment Number to filter results with")
   var paymentNumber: String,
) : SortableRequestBase<PaymentReportFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "bankNumber" to bankNumber,
         "paymentNumber" to paymentNumber,

      )
}
