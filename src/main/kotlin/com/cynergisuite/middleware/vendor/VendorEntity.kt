package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodType
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardType
import com.cynergisuite.middleware.shipping.shipvia.ShipViaEntity
import com.cynergisuite.middleware.vendor.group.VendorGroupEntity
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import java.math.BigDecimal

data class VendorEntity(
   val id: Long? = null,
   val company: Company,
   val name: String, // 30 max
   val address: AddressEntity?,
   val ourAccountNumber: Int?,
   val payTo: Identifiable?, // another vendor
   val freightOnboardType: FreightOnboardType,
   val paymentTerm: VendorPaymentTermEntity,
   val floatDays: Int?,
   val normalDays: Int?,
   val returnPolicy: Boolean,
   val shipVia: ShipViaEntity,
   val vendorGroup: VendorGroupEntity?,
   val minimumQuantity: Int?,
   val minimumAmount: BigDecimal?,
   val freeShipQuantity: Int?,
   val freeShipAmount: BigDecimal?,
   val vendor1099: Boolean = false,
   val federalIdNumber: String?, // 12 max
   val salesRepresentativeName: String?, // 20 max
   val salesRepresentativeFax: String?, // 20 max
   val separateCheck: Boolean = false,
   val bumpPercent: BigDecimal?,
   val freightCalcMethodType: FreightCalcMethodType,
   val freightPercent: BigDecimal?,
   val freightAmount: BigDecimal?,
   val chargeInventoryTax1: Boolean = false,
   val chargeInventoryTax2: Boolean = false,
   val chargeInventoryTax3: Boolean = false,
   val chargeInventoryTax4: Boolean = false,
   val federalIdNumberVerification: Boolean = false,
   val emailAddress: String?, // New Field 320 max
   val purchaseOrderSubmitEmailAddress: String?,
   val allowDropShipToCustomer: Boolean = false,
   val autoSubmitPurchaseOrder: Boolean = false,
   val number: Int? = null
) : Identifiable {

   constructor(id: Long? = null, dto: VendorDTO, vendorPaymentTerm: VendorPaymentTermEntity, shipVia: ShipViaEntity, vendorGroup: VendorGroupEntity?, company: Company, freightOnboardType: FreightOnboardType, freightMethodType: FreightCalcMethodType, payTo: Identifiable? = null) :
      this(
         id = id ?: dto.id,
         company = company,
         name = dto.name!!,
         address = dto.address?.let { AddressEntity(it) },
         ourAccountNumber = dto.ourAccountNumber,
         payTo = payTo,
         freightOnboardType = freightOnboardType,
         paymentTerm = vendorPaymentTerm,
         floatDays = dto.floatDays,
         normalDays = dto.normalDays,
         returnPolicy = dto.returnPolicy!!,
         shipVia = shipVia,
         vendorGroup = vendorGroup,
         minimumQuantity = dto.minimumQuantity,
         minimumAmount = dto.minimumAmount,
         freeShipQuantity = dto.freeShipQuantity,
         freeShipAmount = dto.freeShipAmount,
         vendor1099 = dto.vendor1099!!,
         federalIdNumber = dto.federalIdNumber,
         salesRepresentativeName = dto.salesRepresentativeName,
         salesRepresentativeFax = dto.salesRepresentativeFax,
         separateCheck = dto.separateCheck!!,
         bumpPercent = dto.bumpPercent,
         freightCalcMethodType = freightMethodType,
         freightPercent = dto.freightPercent,
         freightAmount = dto.freightAmount,
         chargeInventoryTax1 = dto.chargeInventoryTax1!!,
         chargeInventoryTax2 = dto.chargeInventoryTax2!!,
         chargeInventoryTax3 = dto.chargeInventoryTax3!!,
         chargeInventoryTax4 = dto.chargeInventoryTax4!!,
         federalIdNumberVerification = dto.federalIdNumberVerification!!,
         emailAddress = dto.emailAddress,
         purchaseOrderSubmitEmailAddress = dto.purchaseOrderSubmitEmailAddress,
         allowDropShipToCustomer = dto.allowDropShipToCustomer!!,
         autoSubmitPurchaseOrder = dto.autoSubmitPurchaseOrder!!
      )

   constructor(existingVendor: VendorEntity, vendorPaymentTerm: VendorPaymentTermEntity, shipVia: ShipViaEntity, dto: VendorDTO, vendorGroup: VendorGroupEntity?, freightOnboardType: FreightOnboardType, freightMethodType: FreightCalcMethodType, payTo: Identifiable? = null) :
      this(
         id = existingVendor.id,
         dto = dto,
         vendorPaymentTerm = vendorPaymentTerm,
         shipVia = shipVia,
         vendorGroup = vendorGroup,
         company = existingVendor.company,
         freightOnboardType = freightOnboardType,
         freightMethodType = freightMethodType,
         payTo = payTo
      )

   override fun myId(): Long? = id
}
