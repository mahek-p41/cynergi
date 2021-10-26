package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.extensions.sumByBigDecimal
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayablePaymentReportTemplate", title = "Account Payable Payment Report Template", description = "Account Payable Payment Report Template")
data class AccountPayablePaymentReportTemplate(

   @field:Schema(description = "Total of AP payment amount for all payments on report")
   var reportTotal: BigDecimal? = null,

   @field:Schema(description = "Listing of payments")
   var payments: List<AccountPayablePaymentReportDTO>? = null

) {
   constructor(entities: List<AccountPayablePaymentEntity>) :
      this(
         payments = entities.asSequence().map { paymentEntity ->
            AccountPayablePaymentReportDTO(paymentEntity)
         }.toList(),
         reportTotal = entities.sumByBigDecimal{ it.amount }
      )
}
