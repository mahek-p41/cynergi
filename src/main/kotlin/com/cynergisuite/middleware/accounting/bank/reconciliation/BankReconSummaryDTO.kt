package com.cynergisuite.middleware.accounting.bank.reconciliation

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@Schema(name = "BankReconSummaryDTO", title = "Bank Reconciliation Summary Totals", description = "Totals for each bank type")
data class BankReconSummaryDTO(

   @field:NotNull
   @field:Schema(description = "Date one amount")
   var ach: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Date two amount")
   var check: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Date four amount")
   var deposit: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Date one amount")
   var fee: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Date one amount")
   var interest: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Date one amount")
   var misc: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Date one amount")
   var serviceCharge: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Date five amount")
   var transfer: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Date one amount")
   var returnCheck: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Date three amount")
   var void: BigDecimal = BigDecimal.ZERO,
) {
   constructor(entity: BankReconSummaryEntity) :
      this(
         ach = entity.ach,
         check = entity.check,
         deposit = entity.deposit,
         fee = entity.fee,
         interest = entity.interest,
         misc = entity.misc,
         serviceCharge = entity.serviceCharge,
         transfer = entity.transfer,
         returnCheck = entity.returnCheck,
         void = entity.void
      )
}

