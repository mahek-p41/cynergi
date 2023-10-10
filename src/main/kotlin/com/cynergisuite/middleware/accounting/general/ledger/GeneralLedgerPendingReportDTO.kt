package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "GeneralLedgerPendingReport", title = "General Ledger Pending Report", description = "General Ledger Pending Report")
data class GeneralLedgerPendingReportDTO(

   @field:Schema(description = "General Ledger Detail ID")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Account number")
   var accountNumber: Long? = null,

   @field:NotNull
   @field:Schema(description = "Account Description")
   var accountName: String? = null,

   @field:NotNull
   @field:Schema(description = "Profit Center ID")
   var profitCenterNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Profit Center Name")
   var profitCenterName: String? = null,

   @field:NotNull
   @field:Schema(description = "Source Code")
   var sourceCode: String? = null,

   @field:NotNull
   @field:Schema(description = "Source Code")
   var sourceCodeDesc: String? = null,

   @field:NotNull
   @field:Schema(description = "Debit Amount")
   var debitAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Credit Amount")
   var creditAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Message")
   var message: String? = null,

   @field:NotNull
   @field:Schema(description = "Journal Entry Number")
   val journalEntryNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Date")
   val date: LocalDate? = null

)  : Identifiable {
   constructor(entity: GeneralLedgerJournalEntity) :
      this(
         id = entity.id,
         accountNumber = entity.account.number,
         accountName = entity.account.name,
         profitCenterNumber = entity.profitCenter.myNumber(),
         profitCenterName = entity.profitCenter.myName(),
         sourceCode = entity.source.value,
         sourceCodeDesc = entity.source.description,
         message = entity.message,
         debitAmount = if (entity.amount >= BigDecimal.ZERO) entity.amount else null,
         creditAmount = if (entity.amount < BigDecimal.ZERO) entity.amount else null,
         date = entity.date
      )

   override fun myId(): UUID? = id
}