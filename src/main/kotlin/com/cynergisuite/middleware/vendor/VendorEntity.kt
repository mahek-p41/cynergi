package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.freight.method.FreightMethodTypeEntity
import com.cynergisuite.middleware.vendor.freight.onboard.FreightOnboardTypeEntity
import java.math.BigDecimal
import java.time.OffsetDateTime

data class VendorEntity(
   val id: Long? = null,
   val company: Company,
   val vendorNumber: Int = 0,
   val nameKey: String, //30 max
   val addressId: Int,
   val ourAccountNumber: Int = 0,
   val payTo: Int = 0,
   val freightOnboardType: FreightOnboardTypeEntity,
   val paymentTermsId: Int, //VendorTerm id
   val floatDays: Int? = 0,
   val normalDays: Int? = 0,
   val returnPolicy: Boolean?,
   val shipViaId: Int,
   val vendorGroupId: Int?,
   val shutdownFrom: OffsetDateTime?,
   val shutdownThru: OffsetDateTime?,
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
   val chargeInvTax1: String,
   val chargeInvTax2: String,
   val chargeInvTax3: String,
   val chargeInvTax4: String,
   val federalIdNumberVerification: Boolean = false,
   val emailAddress: String? //New Field 320 max
) : Identifiable {

   constructor(id: Long? = null, vo: VendorValueObject, company: Company, freightOnboardType: FreightOnboardTypeEntity, freightMethodType: FreightMethodTypeEntity) :
      this(
         id = id ?: vo.id,
         company = company,
         vendorNumber = vo.vendorNumber!!,
         nameKey = vo.nameKey!!,
         addressId = vo.addressId!!,
         ourAccountNumber = vo.ourAccountNumber!!,
         payTo = vo.payTo!!,
         freightOnboardType = freightOnboardType,
         paymentTermsId = vo.paymentTermsId!!,
         floatDays = vo.floatDays,
         normalDays = vo.normalDays,
         returnPolicy = vo.returnPolicy,
         shipViaId = vo.shipViaId!!,
         vendorGroupId = vo.vendorGroupId,
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

   /* Before most !! above
   constructor(id: Long? = null, vo: VendorValueObject, company: Company) :
      this(
         id = id ?: vo.id,
         company = company,
         vendorNumber = vo.vendorNumber,
         nameKey = vo.nameKey!!,
         addressId = vo.addressId,
         ourAccountNumber = vo.ourAccountNumber,
         payTo = vo.payTo,
         freightOnBoardTypeId = vo.freightOnBoardTypeId!!,
         paymentTermsId = vo.paymentTermsId,
         floatDays = vo.floatDays,
         normalDays = vo.normalDays,
         returnPolicy = vo.returnPolicy,
         shipViaId = vo.shipViaId,
         vendorGroupId = vo.vendorGroupId,
         shutdownFrom = vo.shutdownFrom,
         shutdownThru = vo.shutdownThru,
         minimumQuantity = vo.minimumQuantity,
         minimumAmount = vo.minimumAmount,
         freeShipQuantity = vo.freeShipQuantity,
         freeShipAmount = vo.freeShipAmount,
         vend1099 = vo.vendor1099,
         federalIdNumber = vo.federalIdNumber,
         salesRepName = vo.salesRepName,
         salesRepFax = vo.salesRepFax,
         separateCheck = vo.separateCheck,
         bumpPercent = vo.bumpPercent,
         freightCalcMethodType = vo.freightCalcMethodType,
         freightPercent = vo.freightPercent,
         freightAmount = vo.freightAmount,
         chargeInvTax1 = vo.chargeInvTax1,
         chargeInvTax2 = vo.chargeInvTax2,
         chargeInvTax3 = vo.chargeInvTax3,
         chargeInvTax4 = vo.chargeInvTax4,
         federalIdNumberVerification = vo.federalIdNumberVerification,
         emailAddress = vo.emailAddress
      )

    */

   //constructor(source: VendorEntity, updateWith: VendorValueObject) :
   //   this(id = source.id!!, vo = updateWith, company = source.company)

   override fun myId(): Long? = id

}
