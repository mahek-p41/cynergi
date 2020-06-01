package com.cynergisuite.middleware.vendor.payment.term.schedule

import com.cynergisuite.middleware.company.Company
import java.math.BigDecimal

object VendorPaymentTermScheduleTestDataLoader {

   @JvmStatic
   fun single90DaysPayment(): MutableList<VendorPaymentTermScheduleEntity> {
      return mutableListOf(
         VendorPaymentTermScheduleEntity(
            id = null,
            dueMonth = null,
            dueDays = 90,
            duePercent = BigDecimal(0.20),
            scheduleOrderNumber = 1
         )
      )
   }

   @JvmStatic
   fun twoMonthPayments(): MutableList<VendorPaymentTermScheduleEntity> {
      return mutableListOf(
         VendorPaymentTermScheduleEntity(
            id = null,
            dueMonth = 2,
            dueDays = 1,
            duePercent = BigDecimal(0.50),
            scheduleOrderNumber = 1
         ),
         VendorPaymentTermScheduleEntity(
            id = null,
            dueMonth = 4,
            dueDays = 1,
            duePercent = BigDecimal(0.50),
            scheduleOrderNumber = 2
         )
      )
   }
}
