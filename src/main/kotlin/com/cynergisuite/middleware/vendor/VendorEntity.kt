package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.Company
import com.github.javafaker.Bool
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

class VendorEntity(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val company: Company,
   val vendorNumber: Int = 0,
   val nameKey: String, //30 max
   val addressId: Int,
   val ourAccountNumber: Int = 0,
   val payTo: Int = 0,
   //val buyer: String?, REMOVED?
   val freightOnBoardTypeId: Int?, //Was freightOnBoard as a String
   val paymentTermsId: Int, //VendorTerms id
   val floatDays: Int? = 0,
   val normalDays: Int? = 0,
   val returnPolicy: Boolean?, //Was string
   //val nextAccountPayable: Int? = 0, REMOVED?
   val shipViaId: Int,
   val vendorGroupId: Int?, //Was vendorGroup: String?
   val shutdownFrom: OffsetDateTime?,
   val shutdownThru: OffsetDateTime?,
   val minimumQuantity: Int?,
   val minimumAmount: BigDecimal?,
   val freeShipQuantity: Int?,
   val freeShipAmount: BigDecimal?,
   val vendor1099: Boolean = false,
   val federalIdNumber: String?, //12 max
   //val ytdPurchases: BigDecimal?, REMOVED?
   //val lastYearPurchases: BigDecimal?, REMOVED?
   //val balance: BigDecimal?, REMOVED?
   //val ytdDiscounts: BigDecimal?, REMOVED?
   //val lastPayment: OffsetDateTime?, REMOVED?
   val salesRepName: String?, //20 max
   val salesRepFax: String?, //20 max
   val separateCheck: Boolean = false,
   val bumpPercent: BigDecimal?,
   val freightCalcMethodType : Int?, //Was freightCalcMethod : String?
   val freightPercent: BigDecimal?,
   val freightAmount: BigDecimal?,
   //val rebateCode1: Int?, All rebateCode# REMOVED?
   //val rebateCode2: Int?,
   //val rebateCode3: Int?,
   //val rebateCode4: Int?,
   //val rebateCode5: Int?,
   val chargeInvTax1: String,
   val chargeInvTax2: String,
   val chargeInvTax3: String,
   val chargeInvTax4: String,
   val federalIdNumberVerification: Boolean = false,
   val emailAddress: String? //New Field 320 max
) : Identifiable {

   constructor(id: Long, vendor: VendorEntity) :
      this(
         id = id,
         company = vendor.company,
         vendorNumber = vendor.vendorNumber,
         nameKey = vendor.nameKey,
         addressId = vendor.addressId,
         ourAccountNumber = vendor.ourAccountNumber,
         payTo = vendor.payTo,
         freightOnBoardTypeId = vendor.freightOnBoardTypeId,
         paymentTerms = vendor.paymentTermsId,
         floatDays = vendor.floatDays,
         normalDays = vendor.normalDays,
         returnPolicy = vendor.returnPolicy,
         shipViaId = vendor.shipViaId,
         vendorGroup = vendor.vendorGroupId,
         shutdownFrom = vendor.shutdownFrom,
         shutdownThru = vendor.shutdownThru,
         minimumQuantity = vendor.minimumQuantity,
         minimumAmount = vendor.minimumAmount,
         freeShipQuantity = vendor.freeShipQuantity,
         freeShipAmount = vendor.freeShipAmount,
         vendor1099 = vendor.vendor1099,
         federalIdNumber = vendor.federalIdNumber,
         salesRepName = vendor.salesRepName,
         salesRepFax = vendor.salesRepFax,
         separateCheck = vendor.separateCheck,
         bumpPercent = vendor.bumpPercent,
         freightCalcMethodType = vendor.freightCalcMethodType,
         freightPercent = vendor.freightPercent,
         freightAmount = vendor.freightAmount,
         chargeInvTax1 = vendor.chargeInvTax1,
         chargeInvTax2 = vendor.chargeInvTax2,
         chargeInvTax3 = vendor.chargeInvTax3,
         chargeInvTax4 = vendor.chargeInvTax4,
         federalIdNumberVerification = vendor.federalIdNumberVerification,
         emailAddress = vendor.emailAddress
      )

   constructor(vo: VendorValueObject, company: Company) :
      this(
         id = vo.id,
         company = company,
         vendorNumber = vo.vendorNumber,
         nameKey = vo.nameKey,
         addressId = vo.addressId,
         ourAccountNumber = vo.ourAccountNumber,
         payTo = vo.payTo,
         freightOnBoardTypeId = vo.freightOnBoardTypeId,
         paymentTerms = vo.paymentTermsId,
         floatDays = vo.floatDays,
         normalDays = vo.normalDays,
         returnPolicy = vo.returnPolicy,
         shipViaId = vo.shipViaId,
         vendorGroup = vo.vendorGroupId,
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
         freightAmt = vo.freightAmount,
         chargeInvTax1 = vo.chargeInvTax1,
         chargeInvTax2 = vo.chargeInvTax2,
         chargeInvTax3 = vo.chargeInvTax3,
         chargeInvTax4 = vo.chargeInvTax4,
         federalIdNumberVerification = vo.federalIdNumberVerification,
         emailAddress = vo.emailAddress
      )

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): VendorEntity = copy()
}
