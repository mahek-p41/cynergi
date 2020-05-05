package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.vendor.freight.method.FreightMethodTypeValueObject
import com.cynergisuite.middleware.vendor.freight.onboard.FreightOnboardTypeValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.OffsetDateTime
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
data class VendorValueObject(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "vendorNumber", minimum = "1", required = false, description = "Vendor Number")
   var vendorNumber: Int? = null,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 1, max = 30)
   @field:Schema(name = "nameKey", description = "The name key associated with the vendor", minLength = 1, maxLength = 30)
   var nameKey: String? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "addressId", description = "The id associated with the vendor address", minimum = "1", required = false)
   var addressId: Int? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "ourAccountNumber", description = "The vendor account number", minimum = "0", required = false)
   var ourAccountNumber: Int? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "payTo", description = "Pay to vendor", minimum = "0", required = false)
   var payTo: Int? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "freightOnboardType", description = "Vendor freight onboard type")
   var freightOnboardType: FreightOnboardTypeValueObject,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "paymentTermsId", description = "The id associated with the vend_terms record", minimum = "1", required = false)
   var paymentTermsId: Int? = null,

   @field:Positive
   @field:Schema(name = "floatDays", description = "The vendor float days", minimum = "1", required = false)
   var floatDays: Int? = null,

   @field:Positive
   @field:Schema(name = "normalDays", description = "The vendor normal days", minimum = "1", required = false)
   var normalDays: Int? = null,

   @field:NotNull
   @field:Schema(name = "returnPolicy", description = "Whether this vendor has a return policy", example = "true", defaultValue = "false")
   var returnPolicy: Boolean? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "shipViaId", description = "Id link to ship_via", minimum = "0", required = false)
   var shipViaId: Int? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "vendorGroupId", description = "Vendor group", minimum = "0", required = false)
   var vendorGroupId: Int? = null,

   @field:Schema(name = "shutdownFrom", required = false, description = "Beginning date for vendor shut down")
   val shutdownFrom: OffsetDateTime? = null,

   @field:Schema(name = "shutdownThru", required = false, description = "Ending date for vendor shut down")
   val shutdownThru: OffsetDateTime? = null,

   @field:Positive
   @field:Schema(name = "minimumQuantity", description = "Minimum quantity to order from vendor", minimum = "0", required = false)
   var minimumQuantity: Int? = null,

   @field:Positive
   @field:Schema(name = "minimumAmount", description = "Minimum dollar amount to order from vendor")
   var minimumAmount: BigDecimal? = null,

   @field:Positive
   @field:Schema(name = "freeShipQuantity", description = "Quantity to order from vendor for free shipping", minimum = "0", required = false)
   var freeShipQuantity: Int? = null,

   @field:Positive
   @field:Schema(name = "freeShipAmount", description = "Dollar amount to order from vendor for free shipping")
   var freeShipAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(name = "vendor1099", description = "Whether this vendor requires a 1099", example = "true", defaultValue = "false")
   var vendor1099: Boolean? = null,

   @field:Size(min = 0, max = 12)
   @field:Schema(name = "federalIdNumber", description = "Vendor's Federal Identification Number", minLength = 0, maxLength = 12)
   var federalIdNumber: String? = null,

   @field:Size(min = 0, max = 20)
   @field:Schema(name = "salesRepName", description = "Vendor's sales rep name", minLength = 0, maxLength = 20)
   var salesRepName: String? = null,

   @field:Size(min = 0, max = 20)
   @field:Schema(name = "salesRepFax", description = "Vendor's sales rep fax number", minLength = 0, maxLength = 20)
   var salesRepFax: String? = null,

   @field:NotNull
   @field:Schema(name = "separateCheck", description = "Whether this vendor requires separate checks", example = "true", defaultValue = "false")
   var separateCheck: Boolean? = null,

   @field:Schema(name = "bumpPercent", description = "Bump percent")
   val bumpPercent: BigDecimal? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "freightMethodType", description = "Vendor freight method type")
   var freightMethodType: FreightMethodTypeValueObject,

   @field:Schema(name = "freightPercent", description = "Freight percent")
   val freightPercent: BigDecimal? = null,

   @field:Schema(name = "freightAmount", description = "Freight amount")
   val freightAmount: BigDecimal? = null,

   @field:NotNull
   @field:Size(min = 0, max = 300)
   @field:Schema(name = "chargeInvTax1", description = "Chg Inv Tax 1", minLength = 0, maxLength = 300)
   var chargeInvTax1: String? = null,

   @field:NotNull
   @field:Size(min = 0, max = 300)
   @field:Schema(name = "chargeInvTax2", description = "Chg Inv Tax 2", minLength = 0, maxLength = 300)
   var chargeInvTax2: String? = null,

   @field:Size(min = 0, max = 300)
   @field:Schema(name = "chargeInvTax3", description = "Chg Inv Tax 3", minLength = 0, maxLength = 300)
   var chargeInvTax3: String? = null,

   @field:Size(min = 0, max = 300)
   @field:Schema(name = "chargeInvTax4", description = "Chg Inv Tax 4", minLength = 0, maxLength = 300)
   var chargeInvTax4: String? = null,

   @field:NotNull
   @field:Schema(name = "federalIdNumberVerification", description = "Whether vendor has FIN verification", example = "true", defaultValue = "false")
   var federalIdNumberVerification: Boolean? = null,

   @field:Size(min = 0, max = 320)
   @field:Schema(name = "emailAddress", description = "Vendor's Email Address", minLength = 0, maxLength = 320)
   var emailAddress: String? = null

) : Identifiable {

   constructor(entity: VendorEntity) :
      this (
         id = entity.id,
         vendorNumber = entity.vendorNumber,
         nameKey = entity.nameKey,
         addressId = entity.addressId,
         ourAccountNumber = entity.ourAccountNumber,
         payTo = entity.payTo,
         freightOnboardType = FreightOnboardTypeValueObject(entity.freightOnboardType),
         paymentTermsId = entity.paymentTermsId,
         floatDays = entity.floatDays,
         normalDays = entity.normalDays,
         returnPolicy = entity.returnPolicy,
         shipViaId = entity.shipViaId,
         vendorGroupId = entity.vendorGroupId,
         shutdownFrom = entity.shutdownFrom,
         shutdownThru = entity.shutdownThru,
         minimumQuantity = entity.minimumQuantity,
         minimumAmount = entity.minimumAmount,
         freeShipQuantity = entity.freeShipQuantity,
         freeShipAmount = entity.freeShipAmount,
         vendor1099 = entity.vendor1099,
         federalIdNumber = entity.federalIdNumber,
         salesRepName = entity.salesRepName,
         salesRepFax = entity.salesRepFax,
         separateCheck = entity.separateCheck,
         bumpPercent = entity.bumpPercent,
         freightMethodType = FreightMethodTypeValueObject(entity.freightMethodType),
         freightPercent = entity.freightPercent,
         freightAmount = entity.freightAmount,
         chargeInvTax1 = entity.chargeInvTax1,
         chargeInvTax2 = entity.chargeInvTax2,
         chargeInvTax3 = entity.chargeInvTax3,
         chargeInvTax4 = entity.chargeInvTax4,
         federalIdNumberVerification = entity.federalIdNumberVerification,
         emailAddress = entity.emailAddress
      )

   override fun myId(): Long? = id
}
