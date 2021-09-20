package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "AccountPayablePaymentReport", title = "Account Payable Payment Report", description = "Account Payable Payment Report")
data class AccountPayablePaymentReportDTO(

   @field:Schema(description = "Account Payable Payment ID")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Bank number")
   var bankNumber: Long? = null,

   @field:NotNull
   @field:Schema(description = "Vendor number")
   var vendorNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Vendor name")
   var vendorName: String? = null,

   @field:NotNull
   @field:Schema(description = "Account payable payment status id")
   var status: AccountPayablePaymentStatusTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "Account payable payment type id")
   var type: AccountPayablePaymentTypeTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "Account payable payment date")
   var paymentDate: LocalDate? = null,

   @field:Schema(description = "Account payable payment date cleared", required = false)
   var dateCleared: LocalDate? = null,

   @field:Schema(description = "Account payable payment date voided", required = false)
   var dateVoided: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Account payable payment number", maxLength = 20)
   var paymentNumber: String? = null,

   @field:NotNull
   @field:Schema(description = "Account payable payment amount")
   var amount: BigDecimal? = null,

   @field:Schema(description = "Listing of Payment Details associated with this Payment", required = false, accessMode = Schema.AccessMode.READ_ONLY)
   var paymentDetails: MutableSet<AccountPayablePaymentDetailReportDTO> = mutableSetOf()

) : Identifiable {
   constructor(entity: AccountPayablePaymentEntity) :
      this(
         id = entity.id,
         bankNumber = entity.bank.number,
         vendorNumber = entity.vendor.number,
         vendorName = entity.vendor.name,
         status = AccountPayablePaymentStatusTypeDTO(entity.status),
         type = AccountPayablePaymentTypeTypeDTO(entity.type),
         paymentDate = entity.paymentDate,
         dateCleared = entity.dateCleared,
         dateVoided = entity.dateVoided,
         paymentNumber = entity.paymentNumber,
         amount = entity.amount,
         paymentDetails = entity.paymentDetails!!.asSequence().map { paymentDetailEntity ->
            AccountPayablePaymentDetailReportDTO(paymentDetailEntity)
         }.toMutableSet()
      )

   override fun myId(): UUID? = id
}
