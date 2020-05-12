package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.shipvia.ShipViaEntity
import com.cynergisuite.middleware.vendor.freight.method.FreightMethodTypeEntity
import com.cynergisuite.middleware.vendor.freight.onboard.FreightOnboardTypeEntity
import com.cynergisuite.middleware.vendor.group.VendorGroupEntity
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import java.math.BigDecimal
import java.time.LocalDate

data class VendorEntity(
   val id: Long? = null,
   val company: Company,
   val vendorNumber: Int,
   val nameKey: String, //30 max
   val address: AddressEntity,
   val ourAccountNumber: Int = 0,
   val payTo: Identifiable?,
   val freightOnboardType: FreightOnboardTypeEntity,
   val paymentTerm: VendorPaymentTermEntity,
   val floatDays: Int? = 0,
   val normalDays: Int? = 0,
   val returnPolicy: Boolean,
   val shipVia: ShipViaEntity,
   val vendorGroup: VendorGroupEntity,
   val shutdownFrom: LocalDate?,
   val shutdownThru: LocalDate?,
   val minimumQuantity: Int?,
   val minimumAmount: BigDecimal?,
   val freeShipQuantity: Int?,
   val freeShipAmount: BigDecimal?,
   val vendor1099: Boolean = false,
   val federalIdNumber: String?, //12 max
   val salesRepName: String?, //20 max
   val salesRepFax: String?, //20 max
   val separateCheck: Boolean = false,
   val bumpPercent: BigDecimal?,
   val freightMethodType: FreightMethodTypeEntity,
   val freightPercent: BigDecimal?,
   val freightAmount: BigDecimal?,
   val chargeInvTax1: Boolean = false,
   val chargeInvTax2: Boolean = false,
   val chargeInvTax3: Boolean = false,
   val chargeInvTax4: Boolean = false,
   val federalIdNumberVerification: Boolean = false,
   val emailAddress: String? //New Field 320 max
) : Identifiable {

   constructor(id: Long? = null, vo: VendorValueObject, company: Company, freightOnboardType: FreightOnboardTypeEntity, freightMethodType: FreightMethodTypeEntity, payTo: Identifiable? = null) :
      this(
         id = id ?: vo.id,
         company = company,
         vendorNumber = vo.vendorNumber!!,
         nameKey = vo.nameKey!!,
         address = AddressEntity(vo.address),
         ourAccountNumber = vo.ourAccountNumber!!,
         payTo = payTo,
         freightOnboardType = freightOnboardType,
         paymentTerm = VendorPaymentTermEntity(vo.paymentTerm, company),
         floatDays = vo.floatDays,
         normalDays = vo.normalDays,
         returnPolicy = vo.returnPolicy!!,
         shipVia = ShipViaEntity(vo.shipVia, company),
         vendorGroup = VendorGroupEntity(vo.vendorGroup, company),
         shutdownFrom = vo.shutdownFrom,
         shutdownThru = vo.shutdownThru,
         minimumQuantity = vo.minimumQuantity,
         minimumAmount = vo.minimumAmount,
         freeShipQuantity = vo.freeShipQuantity,
         freeShipAmount = vo.freeShipAmount,
         vendor1099 = vo.vendor1099!!,
         federalIdNumber = vo.federalIdNumber,
         salesRepName = vo.salesRepName,
         salesRepFax = vo.salesRepFax,
         separateCheck = vo.separateCheck!!,
         bumpPercent = vo.bumpPercent,
         freightMethodType = freightMethodType,
         freightPercent = vo.freightPercent,
         freightAmount = vo.freightAmount,
         chargeInvTax1 = vo.chargeInvTax1!!,
         chargeInvTax2 = vo.chargeInvTax2!!,
         chargeInvTax3 = vo.chargeInvTax3!!,
         chargeInvTax4 = vo.chargeInvTax4!!,
         federalIdNumberVerification = vo.federalIdNumberVerification!!,
         emailAddress = vo.emailAddress
      )

   override fun myId(): Long? = id

}
