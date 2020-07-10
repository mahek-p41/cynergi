package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import java.math.BigDecimal

data class VendorPaymentTermEntity(
   val id: Long? = null,
   val company: Company,
   val description: String,
   val discountMonth: Int?,
   val discountDays: Int?,
   val discountPercent: BigDecimal?,
   val scheduleRecords: MutableList<VendorPaymentTermScheduleEntity> = mutableListOf()
) : Identifiable {

   constructor(id: Long? = null, vo: VendorPaymentTermDTO, company: Company) :
      this(id = id ?: vo.id,
         company = company,
         description = vo.description!!,
         discountMonth = vo.discountMonth,
         discountDays = vo.discountDays,
         discountPercent = vo.discountPercent,
         scheduleRecords = vo.scheduleRecords.asSequence().map { VendorPaymentTermScheduleEntity(it) }.toMutableList()
      )

   constructor(vo: VendorPaymentTermDTO, company: Company) :
      this(
         id = vo.id,
         company = company,
         description = vo.description!!,
         discountMonth = vo.discountMonth,
         discountDays = vo.discountDays,
         discountPercent = vo.discountPercent,
         scheduleRecords = vo.scheduleRecords.asSequence().map { VendorPaymentTermScheduleEntity(it) }.toMutableList()
      )

   constructor(source: VendorPaymentTermEntity, updateWith: VendorPaymentTermDTO) :
      this(id = source.id!!, vo = updateWith, company = source.company)

   override fun myId(): Long? = id
}
