package com.cynergisuite.middleware.vendor.payment.term.schedule

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.Digits
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
data class VendorPaymentTermScheduleValueObject(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: Long? = null,

   @field:Positive
   @field:Min(value = 1)
   @field:Max(value = 12)
   @field:Schema(name = "dueMonth", minimum = "1", maximum = "12", required = false, description = "Vendor Payment Term Schedule Due Month")
   var dueMonth: Int? = null,

   @field:NotNull
   @field:Positive
   @field:Min(value = 1)
   @field:Schema(name = "dueDays", minimum = "1", required = false, description = "Vendor Payment Term Schedule Due Days")
   var dueDays: Int? = null,

   @field:NotNull
   @field:Positive
   @field:Digits(integer = 1, fraction = 7)
   @field:Schema(name = "duePercent", description = "Vendor Payment Term Schedule Due Percent")
   var duePercent: BigDecimal? = null,

   @field:NotNull
   @field:Positive
   @field:Min(value = 1)
   @field:Max(value = 12)
   @field:Schema(name = "scheduleOrderNumber", minimum = "1", required = false, description = "Payment Order for the Vendor Payment Term")
   var scheduleOrderNumber: Int? = null

) : Identifiable {

   constructor(entity: VendorPaymentTermScheduleEntity) :
      this(
         id = entity.id,
         dueMonth = entity.dueMonth,
         dueDays = entity.dueDays,
         duePercent = entity.duePercent,
         scheduleOrderNumber = entity.scheduleOrderNumber
      )

   override fun myId(): Long? = id
}
