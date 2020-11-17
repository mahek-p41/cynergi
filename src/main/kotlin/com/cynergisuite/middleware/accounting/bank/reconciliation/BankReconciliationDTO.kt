package com.cynergisuite.middleware.accounting.bank.reconciliation

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "Bank Reconciliation", title = "An entity containing a bank reconciliation information", description = "An entity containing a bank reconciliation information.")
data class BankReconciliationDTO(

   @field:Positive
   var id: Long? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(description = "Bank the bank reconciliation is associated with.")
   var bank: SimpleIdentifiableDTO? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(description = "Type the bank reconciliation is associated with.")
   var type: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(description = "Transaction date")
   var date: LocalDate? = null,

   @field:Schema(required = false,description = "Cleared date")
   var clearedDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Bank reconciliation amount")
   var amount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Bank reconciliation description")
   var description: String? = null,

   @field:Valid
   @field:Schema(required = false, description = "The check number in relation to AP Check, system date for SUMGLINT and GLJE.")
   var document: Int? = null

) : Identifiable {
   constructor(entity: BankReconciliationEntity) :
      this(
         id = entity.id,
         bank = SimpleIdentifiableDTO(entity.bank.myId()),
         type = SimpleIdentifiableDTO(entity.type.myId()),
         date = entity.date,
         clearedDate = entity.clearedDate,
         amount = entity.amount,
         description = entity.description,
         document = entity.document
      )

   override fun myId(): Long? = id
}
