package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.vendor.VendorDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "AccountPayablePayment", title = "Account Payable Payment", description = "Account Payable Payment")
data class AccountPayablePaymentDTO(

   @field:Schema(description = "Account Payable Payment id")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Bank id")
   var bank: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(description = "Vendor dto")
   var vendor: VendorDTO? = null,

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
   var paymentDetails: MutableSet<AccountPayablePaymentDetailDTO>? = mutableSetOf()

) : Identifiable {
   constructor(entity: AccountPayablePaymentEntity) :
      this(
         id = entity.id,
         bank = SimpleIdentifiableDTO(entity.bank.id!!, entity.bank.number, entity.bank.name),
         vendor = entity.vendor?.let { VendorDTO(it) },
         status = AccountPayablePaymentStatusTypeDTO(entity.status),
         type = AccountPayablePaymentTypeTypeDTO(entity.type),
         paymentDate = entity.paymentDate,
         dateCleared = entity.dateCleared,
         dateVoided = entity.dateVoided,
         paymentNumber = entity.paymentNumber,
         amount = entity.amount,
         paymentDetails = entity.paymentDetails?.asSequence()?.map { paymentDetailEntity ->
            AccountPayablePaymentDetailDTO(paymentDetailEntity)
         }?.toMutableSet()
      )

   override fun myId(): UUID? = id
}
