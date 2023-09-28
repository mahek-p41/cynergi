package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailEntity
import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "GeneralLedgerSourceReportDetail", title = "General Ledger Source Report Detail", description = "General Ledger Source Report Detail")
data class GeneralLedgerSourceReportDetailDTO(

   @field:Schema(description = "General ledger detail id")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Account number")
   var accountNumber: Long? = null,

   @field:NotNull
   @field:Schema(description = "Account description")
   var accountName: String? = null,

   @field:NotNull
   @field:Schema(description = "Profit center number")
   var profitCenterNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Source code value")
   var sourceCode: String? = null,

   @field:NotNull
   @field:Schema(description = "Source code description")
   var sourceCodeDesc: String? = null,

   @field:NotNull
   @field:Schema(description = "Date")
   var date: LocalDate? = null,

   @field:Schema(description = "Debit amount")
   var debitAmount: BigDecimal? = null,

   @field:Schema(description = "Credit amount")
   var creditAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Message")
   var message: String? = null,

   @field:NotNull
   @field:Schema(description = "Journal entry number")
   var journalEntryNumber: Int? = null,

   @field:Schema(description = "Running total of debit amounts when sorting by description")
   var runningDescTotalDebit: BigDecimal? = BigDecimal.ZERO,

   @field:Schema(description = "Running total of credit amounts when sorting by description")
   var runningDescTotalCredit: BigDecimal? = BigDecimal.ZERO

) : Identifiable {
   constructor(entity: GeneralLedgerDetailEntity) :
      this(
         id = entity.id,
         accountNumber = entity.account.number,
         accountName = entity.account.name,
         profitCenterNumber = entity.profitCenter.myNumber(),
         sourceCode = entity.source.value,
         sourceCodeDesc = entity.source.description,
         date = entity.date,
         debitAmount = if (entity.amount >= BigDecimal.ZERO) entity.amount else null,
         creditAmount = if (entity.amount < BigDecimal.ZERO) entity.amount else null,
         message = entity.message,
         journalEntryNumber = entity.journalEntryNumber
      )

   override fun myId(): UUID? = id
}
