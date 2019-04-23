package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.hightouchinc.cynergi.middleware.dto.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.Digits
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class VerificationAuto(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val address: Boolean?,
   val comment: String?,
   val dealerPhone: String?,
   val diffAddress: String?,
   val diffEmployee: String?,
   val diffPhone: String?,
   val dmvVerify: Boolean?,
   val employer: Boolean?,
   val lastPayment: LocalDate?,
   val name: String?,
   val nextPayment: LocalDate?,
   val note: String?,
   val paymentFrequency: String?,
   val payment: BigDecimal?,
   val pendingAction: String?,
   val phone: Boolean?,
   val previousLoan: Boolean?,
   val purchaseDate: LocalDate?,
   val related: String?,
   val verification: IdentifiableEntity
) : Entity<VerificationAuto> {
   constructor(dto: VerificationAutoDto, verification: IdentifiableEntity) :
      this(
         id = dto.id,
         address = dto.address,
         comment = dto.comment,
         dealerPhone = dto.dealerPhone,
         diffAddress = dto.diffAddress,
         diffEmployee = dto.diffEmployee,
         diffPhone = dto.diffPhone,
         dmvVerify = dto.dmvVerify,
         employer = dto.employer,
         lastPayment = dto.lastPayment,
         name = dto.name,
         nextPayment = dto.nextPayment,
         note = dto.note,
         paymentFrequency = dto.paymentFrequency,
         payment = dto.payment,
         pendingAction = dto.pendingAction,
         phone = dto.phone,
         previousLoan = dto.previousLoan,
         purchaseDate = dto.purchaseDate,
         related = dto.related,
         verification = verification
      )

   override fun entityId(): Long? = id

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): VerificationAuto = copy()

   override fun toString(): String {
      return "VerificationAuto(id=$id, uuRowId=$uuRowId, timeCreated=$timeCreated, timeUpdated=$timeUpdated, address=$address, comment=$comment, dealerPhone=$dealerPhone, diffAddress=$diffAddress, diffEmployee=$diffEmployee, diffPhone=$diffPhone, dmvVerify=$dmvVerify, employer=$employer, lastPayment=$lastPayment, name=$name, nextPayment=$nextPayment, note=$note, paymentFrequency=$paymentFrequency, payment=$payment, pendingAction=$pendingAction, phone=$phone, previousLoan=$previousLoan, purchaseDate=$purchaseDate, related=$related, verification=${verification.entityId()})"
   }
}

@JsonInclude(NON_NULL)
data class VerificationAutoDto(

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
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

) : DataTransferObjectBase<VerificationAutoDto>() {
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

   override fun copyMe(): VerificationAutoDto = copy()

   override fun dtoId(): Long? = id
}
