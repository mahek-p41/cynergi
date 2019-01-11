package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.POSITIVE_NUMBER_REQUIRED
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.validation.constraints.Digits
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class ChecklistAuto(
   var id: Long? = null,
   var uuRowId: UUID,
   var timeCreated: LocalDateTime,
   var timeUpdated: LocalDateTime,
   var address: Boolean = false,
   var comment: String?,
   var dealerPhone: String?,
   var diffAddress: String?,
   var diffEmployee: String?,
   var diffPhone: String?,
   var dmvVerify: Boolean = false,
   var employer: Boolean = false,
   var lastPayment: LocalDate,
   var name: String?,
   var nextPayment: LocalDate?,
   var note: String?,
   var paymentFrequency: String?,
   var payment: BigDecimal?,
   var pendingAction: String?,
   var phone: Boolean = false,
   var previousLoan: Boolean = false,
   var purchaseDate: LocalDate?,
   var related: String?

) : Entity {



   override fun entityId(): Long? = id
}

data class ChecklistAutoDto(

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   var address: Boolean = false,

   @field:Size(max = 100)
   var comment: String?,

   @field:Size(max = 18)
   var dealerPhone: String?,

   @field:Size(max = 50)
   var diffAddress: String?,

   @field:Size(max = 50)
   var diffEmployee: String?,

   @field:Size(max = 18)
   var diffPhone: String?,

   var dmvVerify: Boolean = false,

   var employer: Boolean = false,

   var lastPayment: LocalDate?,

   @field:Size(max = 50)
   var name: String?,

   var nextPayment: LocalDate?,

   @field:Size(max = 50)
   var note: String?,

   @field:Size(max = 10)
   var paymentFrequency: String?,

   @field:Digits(integer = 19, fraction = 2)
   var payment: BigDecimal?,

   @field:Size(max = 50)
   var pendingAction: String?,

   var phone: Boolean = false,

   var previousLoan: Boolean = false,

   var purchaseDate: LocalDate?,

   @field:Size(max = 50)
   var related: String?

)
