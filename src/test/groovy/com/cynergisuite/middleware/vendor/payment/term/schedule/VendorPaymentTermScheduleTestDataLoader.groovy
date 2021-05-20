package com.cynergisuite.middleware.vendor.payment.term.schedule

import groovy.transform.CompileStatic

@CompileStatic
class VendorPaymentTermScheduleTestDataLoader {

   static List<VendorPaymentTermScheduleEntity> single90DaysPayment() {
      return [
         new VendorPaymentTermScheduleEntity(
            null,
            null,
            90,
            new BigDecimal("0.20"),
            1
         )
      ]
   }

   static List<VendorPaymentTermScheduleEntity> twoMonthPayments() {
      return [
         new VendorPaymentTermScheduleEntity(
            null,
            2,
            1,
            new BigDecimal("0.50"),
            1
         ),
         new VendorPaymentTermScheduleEntity(
            null,
            4,
            1,
            new BigDecimal("0.50"),
            2
         )
      ]
   }
}
