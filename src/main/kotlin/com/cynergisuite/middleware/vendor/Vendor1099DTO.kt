package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.address.AddressDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "Vendor1099DTO", title = "An entity containing vendor 1099 information", description = "An entity containing vendor 1099 information.")
data class Vendor1099DTO(

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 1, max = 30)
   @field:Schema(name = "name", description = "The name key associated with the vendor", minLength = 1, maxLength = 30)
   var vendorName: String? = null,

   @field:NotNull
   @field:Schema(name = "vendorNumber", description = "Vendor Number")
   var vendorNumber: Int? = null,

   @field:Valid
   @field:Schema(name = "vendorAddress", description = "Vendor Address")
   var vendorAddress: AddressDTO? = null,

   @field:Valid
   @field:Schema(name = "companyAddress", description = "Company Address")
   var companyAddress: AddressDTO? = null,

   @field:Size(min = 0, max = 12)
   @field:Schema(name = "federalIdNumber", description = "Vendor's Federal Identification Number", minLength = 0, maxLength = 12)
   var federalIdNumber: String? = null,

   @field:NotNull
   @field:Schema(name = "form1099Field", description = "Field Number")
   var form1099Field: Int? = null,

   @field:NotNull
   @field:Size(max = 20)
   @field:Schema(description = "Account payable invoice", maxLength = 20)
   var invoice: String? = null,

   @field:Schema(description = "Account payable payment payment date", required = false)
   var apPaymentPaymentDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Account Description")
   var accountName: String? = null,

   @JsonInclude(NON_DEFAULT)
   @field:Schema(name = "accountNumber", description = "The vendor account number", required = false)
   var accountNumber: String? = null,

   @field:Positive
   @field:Schema(name = "distributionAmount", description = "AP invoice distribution amount")
   var distributionAmount: BigDecimal? = null,

   @field:Schema(name = "Active vendor indicator", required = false, description = "Active vendor indicator")
   var isActive: Boolean = true

) : Identifiable {

   constructor(entity: Vendor1099Entity) :
      this (
         vendorName = entity.vendorName,
         vendorNumber = entity.vendorNumber,
         vendorAddress = entity.vendorAddress?.let { AddressDTO(it) },
         companyAddress = entity.companyAddress?.let { AddressDTO(it) },
         federalIdNumber = entity.federalIdNumber,
         form1099Field = entity.form1099Field,
         invoice = entity.invoice,
         apPaymentPaymentDate = entity.apPaymentPaymentDate,
         accountName = entity.accountName,
         accountNumber = entity.accountNumber,
         isActive = entity.isActive
      )

}
