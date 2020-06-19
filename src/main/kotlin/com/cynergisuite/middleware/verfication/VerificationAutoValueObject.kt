package com.cynergisuite.middleware.verfication

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.Digits
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "VerificationAuto", title = "Automobile verification for a customer", description = "Automobile verification for a single customer associated with a Verification")
data class VerificationAutoValueObject(

   @field:Positive
   var id: Long? = null,

   @field:JsonProperty("auto_address")
   var address: Boolean?,

   @field:Size(max = 100)
   @field:JsonProperty("auto_comment")
   var comment: String?,

   @field:Size(max = 18)
   @field:JsonProperty("auto_dealer_phone")
   var dealerPhone: String?,

   @field:Size(max = 50)
   @field:JsonProperty("auto_diff_address")
   var diffAddress: String?,

   @field:Size(max = 50)
   @field:JsonProperty("auto_diff_emp")
   var diffEmployee: String?,

   @field:Size(max = 18)
   @field:JsonProperty("auto_diff_phone")
   var diffPhone: String?,

   @field:JsonProperty("auto_dmv_verify")
   var dmvVerify: Boolean?,

   @field:JsonProperty("auto_employer")
   var employer: Boolean?,

   @field:JsonProperty("auto_last_payment")
   var lastPayment: LocalDate?,

   @field:Size(max = 50)
   @field:JsonProperty("auto_name")
   var name: String?,

   @field:JsonProperty("auto_next_payment")
   var nextPayment: LocalDate?,

   @field:Size(max = 50)
   @field:JsonProperty("auto_note")
   var note: String?,

   @field:Size(max = 10)
   @field:JsonProperty("auto_pay_freq")
   var paymentFrequency: String?,

   @field:Digits(integer = 19, fraction = 2)
   @field:JsonProperty("auto_payment")
   var payment: BigDecimal?,

   @field:Size(max = 50)
   @field:JsonProperty("auto_pending_action")
   var pendingAction: String?,

   @field:JsonProperty("auto_phone")
   var phone: Boolean?,

   @field:JsonProperty("auto_prev_loan")
   var previousLoan: Boolean?,

   @field:JsonProperty("auto_purchase_date")
   var purchaseDate: LocalDate?,

   @field:Size(max = 50)
   @field:JsonProperty("auto_related")
   var related: String?

) : Identifiable {
   constructor(entity: VerificationAuto) :
      this(
         id = entity.id,
         address = entity.address,
         comment = entity.comment,
         dealerPhone = entity.dealerPhone,
         diffAddress = entity.diffAddress,
         diffEmployee = entity.diffEmployee,
         diffPhone = entity.diffPhone,
         dmvVerify = entity.dmvVerify,
         employer = entity.employer,
         lastPayment = entity.lastPayment,
         name = entity.name,
         nextPayment = entity.nextPayment,
         note = entity.note,
         paymentFrequency = entity.paymentFrequency,
         payment = entity.payment,
         pendingAction = entity.pendingAction,
         phone = entity.phone,
         previousLoan = entity.previousLoan,
         purchaseDate = entity.purchaseDate,
         related = entity.related
      )

   override fun myId(): Long? = id
}
