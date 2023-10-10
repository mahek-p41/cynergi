package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.store.StoreDTO
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull

@Introspected
@Schema(name = "GeneralLedgerJournal", title = "Defines a general ledger journal", description = "Defines a general ledger journal")
data class GeneralLedgerJournalDTO(

   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(name = "account", description = "Account ID")
   var account: AccountDTO? = null,

   @field:NotNull
   @field:Schema(name = "profitCenter", description = "Profit center ID")
   var profitCenter: StoreDTO? = null,

   @field:NotNull
   @field:Schema(name = "date", description = "Date")
   var date: LocalDate? = null,

   @field:NotNull
   @field:Schema(name = "source", description = "General ledger source code")
   var source: GeneralLedgerSourceCodeDTO? = null,

   @field:NotNull
   @field:Schema(name = "amount", description = "Amount")
   var amount: BigDecimal? = null,

   @field:Schema(name = "message", required = false, description = "Message")
   var message: String? = null

) : Identifiable {
   constructor(entity: GeneralLedgerJournalEntity) :
      this(
         id = entity.id,
         account = AccountDTO(entity.account),
         profitCenter = StoreDTO(entity.profitCenter),
         date = entity.date,
         source = GeneralLedgerSourceCodeDTO(entity.source),
         amount = entity.amount,
         message = entity.message
      )

   override fun myId(): UUID? = id
}