package com.cynergisuite.middleware.vendor.payment.terms

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.company.Company
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class VendorPaymentTermsEntity(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val company: Company,
   val number: Int,
   val numberOfPayments: Int,
   val dueMonth1: Int?,
   val dueMonth2: Int?,
   val dueMonth3: Int?,
   val dueMonth4: Int?,
   val dueMonth5: Int?,
   val dueMonth6: Int?,
   val dueDays1: Int?,
   val dueDays2: Int?,
   val dueDays3: Int?,
   val dueDays4: Int?,
   val dueDays5: Int?,
   val dueDays6: Int?,
   val duePercent1: BigDecimal?,
   val duePercent2: BigDecimal?,
   val duePercent3: BigDecimal?,
   val duePercent4: BigDecimal?,
   val duePercent5: BigDecimal?,
   val duePercent6: BigDecimal?,
   val discountMonth: Int?,
   val discountDays: Int?,
   val discountPercent: BigDecimal?

   ) : Entity<VendorPaymentTermsEntity> {

   constructor(vo: VendorPaymentTermsValueObject, company: Company) :
      this(
         id = vo.id,
         company = company,
         number = vo.number!!,
         numberOfPayments = vo.numberOfPayments!!,
         dueMonth1 = vo.dueMonth1,
         dueMonth2 = vo.dueMonth2,
         dueMonth3 = vo.dueMonth3,
         dueMonth4 = vo.dueMonth4,
         dueMonth5 = vo.dueMonth5,
         dueMonth6 = vo.dueMonth6,
         dueDays1 = vo.dueDays1,
         dueDays2 = vo.dueDays2,
         dueDays3 = vo.dueDays3,
         dueDays4 = vo.dueDays4,
         dueDays5 = vo.dueDays5,
         dueDays6 = vo.dueDays6,
         duePercent1 = vo.duePercent1,
         duePercent2 = vo.duePercent2,
         duePercent3 = vo.duePercent3,
         duePercent4 = vo.duePercent4,
         duePercent5 = vo.duePercent5,
         duePercent6 = vo.duePercent6,
         discountMonth = vo.discountMonth,
         discountDays = vo.discountDays,
         discountPercent = vo.discountPercent
      )

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): VendorPaymentTermsEntity = copy()
}
