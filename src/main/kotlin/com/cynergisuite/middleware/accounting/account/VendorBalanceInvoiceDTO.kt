package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.vendor.VendorTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "Account", title = "A data transfer object containing account information", description = "An data transfer object containing a account information.")
data class VendorBalanceInvoiceDTO(

   var id: UUID? = null,

   @field:NotNull
   @field:Schema(name = "name", description = "Description for a Vendor.")
   var expenseDate: LocalDate? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "balance", description = "Vendor Balance")
   var action: String? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "number", description = "Vendor number")
   var invoiceNumber: Long? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "normal account type", description = "Normal account type")
   var invoiceDate: LocalDate? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "normal account type", description = "Normal account type")
   var poNumber: String? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "normal account type", description = "Normal account type")
   var amount: BigDecimal? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "normal account type", description = "Normal account type")
   var balance: BigDecimal? = null,

) : Identifiable {
   constructor(vendorBalanceInvoiceEntity: VendorBalanceInvoiceEntity) :
      this(
         id = vendorBalanceInvoiceEntity.id,
         expenseDate = vendorBalanceInvoiceEntity.expenseDate,
         action = vendorBalanceInvoiceEntity.action,
         invoiceNumber = vendorBalanceInvoiceEntity.invoiceNumber,
         invoiceDate = vendorBalanceInvoiceEntity.invoiceDate,
         poNumber = vendorBalanceInvoiceEntity.poNumber,
         amount = vendorBalanceInvoiceEntity.amount,
         balance = vendorBalanceInvoiceEntity.balance
      )

   override fun myId(): UUID? = id
}
