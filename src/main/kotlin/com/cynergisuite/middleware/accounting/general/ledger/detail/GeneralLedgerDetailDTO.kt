package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.store.StoreDTO
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
@Schema(name = "GeneralLedgerDetail", title = "General Ledger detail", description = "General Ledger detail")
data class GeneralLedgerDetailDTO(

   @field:Schema(description = "General ledger detail ID")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "General ledger detail account")
   var account: AccountDTO?,

   @field:NotNull
   @field:Schema(description = "General ledger detail date")
   var date: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "General ledger profit center")
   var profitCenter: StoreDTO? = null,

   @field:NotNull
   @field:Schema(description = "General ledger source code")
   var source: GeneralLedgerSourceCodeDTO? = null,

   @field:NotNull
   @field:Schema(description = "General ledger detail amount")
   var amount: BigDecimal? = null,

   @field:Schema(nullable = true, description = "General ledger detail message")
   var message: String? = null,

   @field:Schema(nullable = true, description = "General ledger detail employee number ID")
   var employeeNumberId: Int? = null,

   @field:Schema(nullable = true, description = "General ledger detail journal entry number")
   var journalEntryNumber: Int? = null

) : Identifiable {
   constructor(
      entity: GeneralLedgerDetailEntity
   ) :
      this(
         id = entity.id,
         account = AccountDTO(entity.account),
         date = entity.date,
         profitCenter = StoreDTO(entity.profitCenter),
         source = GeneralLedgerSourceCodeDTO(entity.source),
         amount = entity.amount,
         message = entity.message,
         employeeNumberId = entity.employeeNumberId,
         journalEntryNumber = entity.journalEntryNumber
      )

   override fun myId(): UUID? = id
}
