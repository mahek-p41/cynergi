package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "AccountPayablePaymentDetailReportDTO", title = "Account Payable Payment Detail Report", description = "Account Payable Payment Detail Report")
data class AccountPayablePaymentDetailReportDTO(
   @field:NotNull
   @field:Schema(description = "Bank number")
   var bankNumber: Int? = null,
   var paymentType: String? = null,
   var paymentNumber: String? = null,
   var paymentDate: LocalDate? = null,
   var paymentDetailId: String? = null,
   var paymentDetailAmount: BigDecimal? = null,
   )