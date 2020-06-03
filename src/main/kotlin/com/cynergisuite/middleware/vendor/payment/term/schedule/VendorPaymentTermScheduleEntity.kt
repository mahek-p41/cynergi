package com.cynergisuite.middleware.vendor.payment.term.schedule

import com.cynergisuite.domain.Identifiable
import java.math.BigDecimal

data class VendorPaymentTermScheduleEntity(
   val id: Long? = null,
   val dueMonth: Int?,
   val dueDays: Int,
   val duePercent: BigDecimal,
   val scheduleOrderNumber: Int
) : Identifiable {

   constructor(vo: VendorPaymentTermScheduleDTO) :
      this(
         id = vo.id,
         dueMonth = vo.dueMonth,
         dueDays = vo.dueDays!!,
         duePercent = vo.duePercent!!,
         scheduleOrderNumber = vo.scheduleOrderNumber!!
      )

   override fun myId(): Long? = id
}
