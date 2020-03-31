package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
data class VendorPaymentTermValueObject(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Size(min = 3, max = 30)
   @field:Schema(name = "description", minimum = "1", maximum = "30", description = "Describes the vendor payment term")
   var description: String? = null,

   @field:Positive
   @field:Schema(name = "number", minimum = "1", required = false, description = "Vendor Payment Term Number")
   var number: Int? = null,

   @field:Positive
   @field:Schema(name = "number", minimum = "1", required = false, description = "Vendor Payment Term Number of Payments")
   var numberOfPayments: Int? = null,

   @field:Positive
   @field:Schema(name = "dueMonth1", minimum = "1", required = false, description = "Vendor Payment Term Due Month 1")
   var dueMonth1: Int? = null,

   @field:Positive
   @field:Schema(name = "dueMonth2", minimum = "1", required = false, description = "Vendor Payment Term Due Month 2")
   var dueMonth2: Int? = null,

   @field:Positive
   @field:Schema(name = "dueMonth3", minimum = "1", required = false, description = "Vendor Payment Term Due Month 3")
   var dueMonth3: Int? = null,

   @field:Positive
   @field:Schema(name = "dueMonth4", minimum = "1", required = false, description = "Vendor Payment Term Due Month 4")
   var dueMonth4: Int? = null,

   @field:Positive
   @field:Schema(name = "dueMonth5", minimum = "1", required = false, description = "Vendor Payment Term Due Month 5")
   var dueMonth5: Int? = null,

   @field:Positive
   @field:Schema(name = "dueMonth6", minimum = "1", required = false, description = "Vendor Payment Term Due Month 6")
   var dueMonth6: Int? = null,

   @field:Positive
   @field:Schema(name = "dueDays1", minimum = "1", required = false, description = "Vendor Payment Term Due Month 1")
   var dueDays1: Int? = null,

   @field:Positive
   @field:Schema(name = "dueDays2", minimum = "1", required = false, description = "Vendor Payment Term Due Month 2")
   var dueDays2: Int? = null,

   @field:Positive
   @field:Schema(name = "dueDays3", minimum = "1", required = false, description = "Vendor Payment Term Due Month 3")
   var dueDays3: Int? = null,

   @field:Positive
   @field:Schema(name = "dueDays4", minimum = "1", required = false, description = "Vendor Payment Term Due Month 4")
   var dueDays4: Int? = null,

   @field:Positive
   @field:Schema(name = "dueDays5", minimum = "1", required = false, description = "Vendor Payment Term Due Month 5")
   var dueDays5: Int? = null,

   //Does Z have min or max for these?
//Are the due date fields mutually exclusive?
//If we add validation, write tests to prove it worked.

   @field:Positive
   @field:Schema(name = "dueDays6", minimum = "1", required = false, description = "Vendor Payment Term Due Month 6")
   var dueDays6: Int? = null,

   @field:Schema(name = "duePercent1", description = "Vendor Payment Term Due Percent 1")
   var duePercent1: BigDecimal? = null,

   @field:Schema(name = "duePercent2", description = "Vendor Payment Term Due Percent 2")
   var duePercent2: BigDecimal? = null,

   @field:Schema(name = "duePercent3", description = "Vendor Payment Term Due Percent 3")
   var duePercent3: BigDecimal? = null,

   @field:Schema(name = "duePercent4", description = "Vendor Payment Term Due Percent 4")
   var duePercent4: BigDecimal? = null,

   @field:Schema(name = "duePercent5", description = "Vendor Payment Term Due Percent 5")
   var duePercent5: BigDecimal? = null,

   @field:Schema(name = "duePercent6", description = "Vendor Payment Term Due Percent 6")
   var duePercent6: BigDecimal? = null,

   @field:Positive
   @field:Schema(name = "discountMonth", minimum = "1", required = false, description = "Vendor Payment Term Discount Month")
   var discountMonth: Int? = null,

   @field:Positive
   @field:Schema(name = "discountDays", minimum = "1", required = false, description = "Vendor Payment Term Discount Days")
   var discountDays: Int? = null,

   @field:Schema(name = "discountPercent", description = "Vendor Payment Term Discount Percent")
   var discountPercent: BigDecimal? = null

) : Identifiable {

   constructor(entity: VendorPaymentTermEntity) :
      this(
         id = entity.id,
         description = entity.description,
         number = entity.number,
         numberOfPayments = entity.numberOfPayments,
         dueMonth1 = entity.dueMonth1,
         dueMonth2 = entity.dueMonth2,
         dueMonth3 = entity.dueMonth3,
         dueMonth4 = entity.dueMonth4,
         dueMonth5 = entity.dueMonth5,
         dueMonth6 = entity.dueMonth6,
         dueDays1 = entity.dueDays1,
         dueDays2 = entity.dueDays2,
         dueDays3 = entity.dueDays3,
         dueDays4 = entity.dueDays4,
         dueDays5 = entity.dueDays5,
         dueDays6 = entity.dueDays6,
         duePercent1 = entity.duePercent1,
         duePercent2 = entity.duePercent2,
         duePercent3 = entity.duePercent3,
         duePercent4 = entity.duePercent4,
         duePercent5 = entity.duePercent5,
         duePercent6 = entity.duePercent6,
         discountMonth = entity.discountMonth,
         discountDays = entity.discountDays,
         discountPercent = entity.discountPercent
      )

   override fun myId(): Long? = id
}
