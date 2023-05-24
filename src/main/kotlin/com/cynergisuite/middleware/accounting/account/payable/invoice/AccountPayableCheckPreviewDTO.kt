package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "AccountPayableCheckPreviewDTO", title = "Account Payable Check Preview DTO", description = "Account Payable Check Preview DTO")
data class AccountPayableCheckPreviewDTO(

   @field:NotNull
   @field:Schema(description = "List of vendors")
   var vendorList: List<AccountPayableCheckPreviewVendorsDTO>,

   @field:NotNull
   @field:Schema(description = "Gross")
   var gross: BigDecimal,

   @field:NotNull
   @field:Schema(description = "Gross")
   var discount: BigDecimal,

   @field:NotNull
   @field:Schema(description = "Gross")
   var deduction: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Gross")
   var netPaid: BigDecimal? = BigDecimal.ZERO,
) {
   constructor(entity: AccountPayableCheckPreviewEntity) :
      this(
         vendorList = entity.vendorList.asSequence().map { vendorEntity ->
            AccountPayableCheckPreviewVendorsDTO(vendorEntity)}.toMutableList(),
         gross = entity.gross,
         discount = entity.discount,
         deduction = entity.deduction,
         netPaid = entity.netPaid
      )
}
