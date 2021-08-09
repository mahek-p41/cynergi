package com.cynergisuite.middleware.accounting.bank.reconciliation

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "Bank Reconciliation", title = "An entity containing a bank reconciliation information", description = "An entity containing a bank reconciliation information.")
data class BankReconciliationDTO(

   var id: UUID? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(description = "Bank the bank reconciliation is associated with.")
   var bank: SimpleIdentifiableDTO? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(description = "Type the bank reconciliation is associated with.")
   var type: BankReconciliationTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "Transaction date")
   var date: LocalDate? = null,

   @field:Schema(required = false, description = "Cleared date")
   var clearedDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Bank reconciliation amount")
   var amount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Bank reconciliation description")
   var description: String? = null,

   @field:Schema(required = false, description = "The payment number is relation to AP payments (ACH or check), system date for SUMGLINT and GLJE.")
   var document: String? = null

) : Identifiable {
   override fun myId(): UUID? = id
}
