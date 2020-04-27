package com.cynergisuite.middleware.vendor.payment.term.schedule

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

data class VendorPaymentTermScheduleEntity(
   val id: Long? = null,
   val dueMonth: Int?,
   val dueDays: Int,
   val duePercent: BigDecimal,
   val scheduleOrderNumber: Int
) : Identifiable {

   constructor(vo: VendorPaymentTermScheduleValueObject) :
      this(
         id = vo.id,
         dueMonth = vo.dueMonth,
         dueDays = vo.dueDays!!,
         duePercent = vo.duePercent!!,
         scheduleOrderNumber = vo.scheduleOrderNumber!!
      )

   override fun myId(): Long? = id
}
