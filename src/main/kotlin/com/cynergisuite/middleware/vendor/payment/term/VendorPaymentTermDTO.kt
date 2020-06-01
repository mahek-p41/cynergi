package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.Valid
import javax.validation.constraints.Digits
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
data class VendorPaymentTermDTO(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Size(min = 3, max = 30)
   @field:Schema(name = "description", minimum = "1", maximum = "30", description = "Describes the vendor payment term")
   var description: String? = null,

   @field:Positive
   @field:Schema(name = "discountMonth", minimum = "1", required = false, description = "Vendor Payment Term Discount Month")
   var discountMonth: Int? = null,

   @field:Positive
   @field:Schema(name = "discountDays", minimum = "1", required = false, description = "Vendor Payment Term Discount Days")
   var discountDays: Int? = null,

   @field:Positive
   @field:Digits(integer = 1, fraction = 7)
   @field:Schema(name = "discountPercent", description = "Vendor Payment Term Discount Percent")
   var discountPercent: BigDecimal? = null,

   @field:Valid
   @field:Size(min = 1)
   @field:Schema(name = "scheduleRecords", description = "Listing of schedule records associated with a Vendor Payment Term")
   var scheduleRecords: MutableList<VendorPaymentTermScheduleValueObject> = mutableListOf()

) : Identifiable {

   constructor(entity: VendorPaymentTermEntity) :
      this(
         id = entity.id,
         description = entity.description,
         discountMonth = entity.discountMonth,
         discountDays = entity.discountDays,
         discountPercent = entity.discountPercent,
         scheduleRecords = entity.scheduleRecords.asSequence().map { VendorPaymentTermScheduleValueObject(it) }.toMutableList()
      )

   override fun myId(): Long? = id
}
