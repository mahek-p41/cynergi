package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerDetail", title = "General Ledger detail", description = "General Ledger detail")
data class GeneralLedgerDetailDTO(

   @field:Positive
   @field:Schema(description = "General ledger detail ID")
   var id: Long? = null,

   @field:NotNull
   @field:Schema(description = "General ledger detail account")
   var account: SimpleIdentifiableDTO?,

   @field:NotNull
   @field:Schema(description = "General ledger detail date")
   var date: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "General ledger profit center")
   var profitCenter: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(description = "General ledger source code")
   var source: SimpleIdentifiableDTO? = null,

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
         account = SimpleIdentifiableDTO(entity.account),
         date = entity.date,
         profitCenter = SimpleIdentifiableDTO(entity.profitCenter.myId()),
         source = SimpleIdentifiableDTO(entity.source),
         amount = entity.amount,
         message = entity.message,
         employeeNumberId = entity.employeeNumberId,
         journalEntryNumber = entity.journalEntryNumber
      )

   override fun myId(): Long? = id
}
