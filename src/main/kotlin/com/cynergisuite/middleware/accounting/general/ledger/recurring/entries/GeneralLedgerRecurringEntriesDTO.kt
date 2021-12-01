package com.cynergisuite.middleware.accounting.general.ledger.recurring.entries

import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerRecurringEntries", title = "General Ledger Recurring Entries", description = "General ledger recurring entries entity")
data class GeneralLedgerRecurringEntriesDTO(

   @field:Valid
   @field:NotNull
   @field:Schema(description = "General ledger recurring")
   var generalLedgerRecurring: GeneralLedgerRecurringDTO? = null,

   @field:NotEmpty
   @field:Schema(description = "List of general ledger recurring distributions associated with the general ledger recurring")
   var generalLedgerRecurringDistributions: MutableList<GeneralLedgerRecurringDistributionDTO> = mutableListOf(),

   @field:NotNull
   @field:Schema(description = "General ledger distribution amount net balance (should be 0 for records to be posted/updated)")
   var balance: BigDecimal? = null

) {
   constructor(
      entity: GeneralLedgerRecurringEntriesEntity
   ) :
      this(
         generalLedgerRecurring = GeneralLedgerRecurringDTO(entity.generalLedgerRecurring),
         generalLedgerRecurringDistributions = entity.generalLedgerRecurringDistributions.map { GeneralLedgerRecurringDistributionDTO(it) } as MutableList<GeneralLedgerRecurringDistributionDTO>,
         balance = BigDecimal.ZERO
      )
}
