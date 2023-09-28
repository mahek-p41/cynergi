package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.vendor.VendorTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "Account", title = "A data transfer object containing account information", description = "An data transfer object containing a account information.")
data class VendorBalanceDTO(

   var id: UUID? = null,

   @field:NotNull
   @field:Schema(name = "name", description = "Description for a Vendor.")
   var name: String? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "number", description = "Vendor number")
   var number: Long? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "balance", description = "Vendor Balance")
   var balance: BigDecimal? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "normal account type", description = "Normal account type")
   var invoiceList: List<VendorBalanceInvoiceDTO>? = null,

) : Identifiable {
   constructor(vendorBalanceEntity: VendorBalanceEntity) :
      this(
         id = vendorBalanceEntity.id,
         number = vendorBalanceEntity.number,
         name = vendorBalanceEntity.name,
         balance = vendorBalanceEntity.balance,
         invoiceList = vendorBalanceEntity.invoiceList
      )

   override fun myId(): UUID? = id
}
