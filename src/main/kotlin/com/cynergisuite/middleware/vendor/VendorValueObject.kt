package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.OffsetDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "Audit", title = "Single Audit associated with a single Store", description = "A single audit for a store on a specified date along with it's current state")
data class VendorValueObject (

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "vendorNumber", minimum = "1", required = false, description = "Vendor Number")
   var vendorNumber: Int,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 1, max = 30)
   @field:Schema(name = "nameKey", description = "The name key associated with the vendor", minLength = 1, maxLength = 30)
   var nameKey: String? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "addressId", description = "The id associated with the vendor address", minimum = "1", required = false)
   var addressId: Int = 0,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "ourAccountNumber", description = "The vendor account number", minimum = "0", required = false)
   var ourAccountNumber: Int = 0,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "payTo", description = "Pay to vendor", minimum = "0", required = false)
   var payTo: Int = 0,

   @field:NotBlank
   @field:Size(min = 0, max = 1)
   @field:Schema(name = "freightOnBoardTypeId", description = "Vendor fob", minLength = 0, maxLength = 1)
   var freightOnBoardTypeId: String? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "paymentTermsId", description = "The id associated with the vend_terms record", minimum = "1", required = false)
   var paymentTermsId: Int = 0,

   @field:Positive
   @field:Schema(name = "floatDays", description = "The vendor float days", minimum = "1", required = false)
   var floatDays: Int? = 0,

   @field:Positive
   @field:Schema(name = "normalDays", description = "The vendor normal days", minimum = "1", required = false)
   var normalDays: Int? = 0,

   @field:Size(min = 0, max = 300)
   @field:Schema(name = "returnPolicy", description = "Vendor return policy", minLength = 0, maxLength = 300)
   var returnPolicy: String? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "shipViaId", description = "Id link to ship_via", minimum = "0", required = false)
   var shipViaId: Int,

   @field:Size(min = 0, max = 300)
   @field:Schema(name = "vendorGroupId", description = "Vendor group", minLength = 0, maxLength = 300)
   var vendorGroupId: String? = null,

   @field:Schema(name = "shutdownFrom", required = false, description = "Beginning date for vendor shut down")
   val shutdownFrom: OffsetDateTime? = null,

   @field:Schema(name = "shutdownThru", required = false, description = "Ending date for vendor shut down")
   val shutdownThru: OffsetDateTime? = null,

   @field:Positive
   @field:Schema(name = "minimumQuantity", description = "Minimum quantity to order from vendor", minimum = "0", required = false)
   var minimumQuantity: Int?,

   @field:Positive
   @field:Schema(name = "minimumAmount", description = "Minimum dollar amount to order from vendor")
   var minimumAmount: BigDecimal?,

   @field:Positive
   @field:Schema(name = "freeShipQuantity", description = "Quantity to order from vendor for free shipping", minimum = "0", required = false)
   var freeShipQuantity: Int?,

   @field:Positive
   @field:Schema(name = "freeShipAmount", description = "Dollar amount to order from vendor for free shipping")
   var freeShipAmount: BigDecimal?,

   @field:NotNull
   @field:Schema(name = "vendor1099", description = "Whether this vendor requires a 1099", example = "true", defaultValue = "false")
   var vendor1099: Boolean = false,

   @field:Size(min = 0, max = 300)
   @field:Schema(name = "federalIdNumber", description = "Vendor's Federal Identification Number", minLength = 0, maxLength = 300)
   var federalIdNumber: String? = null,

   @field:Size(min = 0, max = 300)
   @field:Schema(name = "salesRepName", description = "Vendor's sales rep name", minLength = 0, maxLength = 300)
   var salesRepName: String? = null,

   @field:Size(min = 0, max = 300)
   @field:Schema(name = "salesRepFax", description = "Vendor's sales rep fax number", minLength = 0, maxLength = 300)
   var salesRepFax: String? = null,

   @field:NotNull
   @field:Schema(name = "separateCheck", description = "Whether this vendor requires separate checks", example = "true", defaultValue = "false")
   var separateCheck: Boolean = false,

   @field:Schema(name = "bumpPercent", description = "Bump percent")
   val bumpPercent: BigDecimal?,

   @field:Size(min = 0, max = 300)
   @field:Schema(name = "freightCalcMethodType", description = "Method for calculating freight", minLength = 0, maxLength = 300)
   var freightCalcMethodType: String? = null,

   @field:Schema(name = "freightPercent", description = "Freight percent")
   val freightPercent: BigDecimal?,

   @field:Schema(name = "freightAmount", description = "Freight amount")
   val freightAmount: BigDecimal?,

   @field:NotNull
   @field:Size(min = 0, max = 300)
   @field:Schema(name = "chargeInvTax1", description = "Chg Inv Tax 1", minLength = 0, maxLength = 300)
   var chargeInvTax1: String,

   @field:NotNull
   @field:Size(min = 0, max = 300)
   @field:Schema(name = "chargeInvTax2", description = "Chg Inv Tax 2", minLength = 0, maxLength = 300)
   var chargeInvTax2: String,

   @field:Size(min = 0, max = 300)
   @field:Schema(name = "chargeInvTax3", description = "Chg Inv Tax 3", minLength = 0, maxLength = 300)
   var chargeInvTax3: String,

   @field:Size(min = 0, max = 300)
   @field:Schema(name = "chargeInvTax4", description = "Chg Inv Tax 4", minLength = 0, maxLength = 300)
   var chargeInvTax4: String,

   @field:Size(min = 0, max = 300)
   @field:Schema(name = "chargeInvTax5", description = "Chg Inv Tax 5", minLength = 0, maxLength = 300)
   var chargeInvTax5: String,

   @field:NotNull
   @field:Schema(name = "federalIdNumberVerification", description = "Whether vendor has FIN verification", example = "true", defaultValue = "false")
   var federalIdNumberVerification: Boolean = false,

   @field:Size(min = 0, max = 320)
   @field:Schema(name = "emailAddress", description = "Vendor's Email Address", minLength = 0, maxLength = 320)
   var emailAddress: String
) : Identifiable {

   constructor(entity: VendorEntity) :
      this (
         id = entity.id,
         vendorNumber = entity.vendorNumber,
         nameKey = entity.nameKey,
         addressId = entity.addressId,
         ourAccountNumber = entity.ourAccountNumber,
         payTo = entity.payTo,
         freightOnboardTypeId = entity.freightOnBoardTypeId,
         paymentTerms = entity.paymentTermsId,
         floatDays = entity.floatDays,
         normalDays = entity.normalDays,
         returnPolicy = entity.returnPolicy,
         shipViaId = entity.shipViaId,
         vendorGroup = entity.vendorGroupId,
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
         freightCalcMethodType = entity.freightCalcMethodType,
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
