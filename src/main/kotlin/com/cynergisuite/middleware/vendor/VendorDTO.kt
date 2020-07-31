package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.address.AddressDTO
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodTypeDTO
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.Valid
import javax.validation.constraints.Digits
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "Vendor", title = "An entity containing vendor information", description = "An entity containing vendor information.")
data class VendorDTO(

        @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: Long? = null,

        @field:NotNull
   @field:NotBlank
   @field:Size(min = 1, max = 30)
   @field:Schema(name = "name", description = "The name key associated with the vendor", minLength = 1, maxLength = 30)
   var name: String? = null,

        @field:Valid
   @field:NotNull
   @field:Schema(name = "address", description = "Bank Address.")
   var address: AddressDTO? = null,

        @field:NotNull
   @field:Positive
   @field:Schema(name = "ourAccountNumber", description = "The vendor account number", minimum = "0", required = false)
   var ourAccountNumber: Int? = null,

        @field:Valid
   @field:Schema(name = "payTo", description = "Pay to vendor.  Provide the ID of a valid vendor for the company that the user is logged in under. Use GET /api/vendor/search{?pageRequest*}", minimum = "1", required = false)
   var payTo: SimpleIdentifiableDTO? = null,

        @field:Valid
   @field:NotNull
   @field:Schema(name = "freightOnboardType", description = "Vendor freight onboard type")
   var freightOnboardType: FreightOnboardTypeDTO? = null,

        @field:Valid
   @field:NotNull
   @field:Schema(name = "paymentTerm", description = "Vendor Payment Term")
   var paymentTerm: SimpleIdentifiableDTO? = null,

        @field:Schema(name = "floatDays", description = "The vendor float days", minimum = "0", required = false)
   var floatDays: Int? = null,

        @field:Schema(name = "normalDays", description = "The vendor normal days", minimum = "0", required = false)
   var normalDays: Int? = null,

        @field:NotNull
   @field:Schema(name = "returnPolicy", description = "Whether this vendor has a return policy", example = "true", defaultValue = "false")
   var returnPolicy: Boolean? = null,

        @field:Valid
   @field:NotNull
   @field:Schema(name = "shipVia", description = "Ship Via", implementation = SimpleIdentifiableDTO::class)
   var shipVia: SimpleIdentifiableDTO? = null,

        @field:Valid
   @field:Schema(name = "vendorGroup", description = "Vendor Group", implementation = SimpleIdentifiableDTO::class)
   var vendorGroup: SimpleIdentifiableDTO? = null,

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
   @field:Schema(name = "salesRepresentativeName", description = "Vendor's sales rep name", minLength = 0, maxLength = 20)
   var salesRepresentativeName: String? = null,

        @field:Size(min = 0, max = 20)
   @field:Schema(name = "salesRepresentativeFax", description = "Vendor's sales rep fax number", minLength = 0, maxLength = 20)
   var salesRepresentativeFax: String? = null,

        @field:NotNull
   @field:Schema(name = "separateCheck", description = "Whether this vendor requires separate checks", example = "true", defaultValue = "false")
   var separateCheck: Boolean? = null,

        @field:Digits(integer = 1, fraction = 7)
   @field:Schema(name = "bumpPercent", description = "Bump percent")
   var bumpPercent: BigDecimal? = null,

        @field:Valid
   @field:NotNull
   @field:Schema(name = "freightCalcMethodType", description = "Vendor freight method type")
   var freightCalcMethodType: FreightCalcMethodTypeDTO? = null,

        @field:Digits(integer = 1, fraction = 7)
   @field:Schema(name = "freightPercent", description = "Freight percent")
   var freightPercent: BigDecimal? = null,

        @field:Schema(name = "freightAmount", description = "Freight amount")
   var freightAmount: BigDecimal? = null,

        @field:NotNull
   @field:Schema(name = "chargeInventoryTax1", description = "Chg Inv Tax 1", example = "true", defaultValue = "false")
   var chargeInventoryTax1: Boolean? = null,

        @field:NotNull
   @field:Schema(name = "chargeInventoryTax2", description = "Chg Inv Tax 2", example = "true", defaultValue = "false")
   var chargeInventoryTax2: Boolean? = null,

        @field:NotNull
   @field:Schema(name = "chargeInventoryTax3", description = "Chg Inv Tax 3", example = "true", defaultValue = "false")
   var chargeInventoryTax3: Boolean? = null,

        @field:NotNull
   @field:Schema(name = "chargeInventoryTax4", description = "Chg Inv Tax 4", example = "true", defaultValue = "false")
   var chargeInventoryTax4: Boolean? = null,

        @field:NotNull
   @field:Schema(name = "federalIdNumberVerification", description = "Whether vendor has FIN verification", example = "true", defaultValue = "false")
   var federalIdNumberVerification: Boolean? = null,

        @field:Email
   @field:Size(min = 0, max = 320)
   @field:Schema(name = "emailAddress", description = "Vendor's Email Address", minLength = 0, maxLength = 320)
   var emailAddress: String? = null,

        @field:Email
   @field:Size(min = 0, max = 320)
   @field:Schema(name = "purchaseOrderSubmitEmailAddress", description = "If available and enabled this is the email address that a purchase order will be submitted to", minLength = 0, maxLength = 320)
   var purchaseOrderSubmitEmailAddress: String? = null,

        @field:NotNull
   @field:Schema(name = "allowDropShipToCustomer", description = "Indicator on if the vendor supports shipping to customer directly", required = true)
   var allowDropShipToCustomer: Boolean? = null,

        @field:NotNull
   @field:Schema(name = "autoSubmitPurchaseOrder", description = "Indicator on if the vendor supports automatically submitting a purchase order.", required = true)
   var autoSubmitPurchaseOrder: Boolean? = null

) : Identifiable {

   constructor(entity: VendorEntity) :
      this (
         id = entity.id,
         name = entity.name,
         address = AddressDTO(entity.address),
         ourAccountNumber = entity.ourAccountNumber,
         payTo = if (entity.payTo != null) SimpleIdentifiableDTO(entity.payTo) else null,
         freightOnboardType = FreightOnboardTypeDTO(entity.freightOnboardType),
         paymentTerm = SimpleIdentifiableDTO(entity.paymentTerm),
         floatDays = entity.floatDays,
         normalDays = entity.normalDays,
         returnPolicy = entity.returnPolicy,
         shipVia = SimpleIdentifiableDTO(entity.shipVia),
         vendorGroup = if (entity.vendorGroup != null) SimpleIdentifiableDTO(entity.vendorGroup.myId()) else null,
         minimumQuantity = entity.minimumQuantity,
         minimumAmount = entity.minimumAmount,
         freeShipQuantity = entity.freeShipQuantity,
         freeShipAmount = entity.freeShipAmount,
         vendor1099 = entity.vendor1099,
         federalIdNumber = entity.federalIdNumber,
         salesRepresentativeName = entity.salesRepresentativeName,
         salesRepresentativeFax = entity.salesRepresentativeFax,
         separateCheck = entity.separateCheck,
         bumpPercent = entity.bumpPercent,
         freightCalcMethodType = FreightCalcMethodTypeDTO(entity.freightCalcMethodType),
         freightPercent = entity.freightPercent,
         freightAmount = entity.freightAmount,
         chargeInventoryTax1 = entity.chargeInventoryTax1,
         chargeInventoryTax2 = entity.chargeInventoryTax2,
         chargeInventoryTax3 = entity.chargeInventoryTax3,
         chargeInventoryTax4 = entity.chargeInventoryTax4,
         federalIdNumberVerification = entity.federalIdNumberVerification,
         emailAddress = entity.emailAddress,
         purchaseOrderSubmitEmailAddress = entity.purchaseOrderSubmitEmailAddress,
         allowDropShipToCustomer = entity.allowDropShipToCustomer,
         autoSubmitPurchaseOrder = entity.autoSubmitPurchaseOrder
      )

   override fun myId(): Long? = id
}
