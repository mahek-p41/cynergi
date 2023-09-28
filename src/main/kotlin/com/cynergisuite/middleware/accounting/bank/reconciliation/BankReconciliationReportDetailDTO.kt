package com.cynergisuite.middleware.accounting.bank.reconciliation

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.bank.BankDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "Bank Reconciliation Report", title = "An entity containing a bank reconciliation report information", description = "An entity containing a bank reconciliation report information.")
data class BankReconciliationReportDetailDTO(

   var id: UUID? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(description = "Bank the bank reconciliation is associated with.")
   var bank: BankDTO? = null,

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
   var document: String? = null,

   @field:Schema(required = false, description = "The vendor name in relation to AP payments (ACH or check), system date for SUMGLINT and GLJE.")
   var vendorName: String? = null,

) : Identifiable {
  constructor(entity: BankReconciliationReportDetailEntity) :
     this(
        id = entity.id,
        bank = BankDTO(entity.bank),
        type = BankReconciliationTypeDTO(entity.type),
        date = entity.date,
        clearedDate = entity.clearedDate,
        amount = entity.amount,
        description = entity.description,
        document = entity.document,
        vendorName = entity.vendorName,
     )

   override fun myId(): UUID? = id
}
