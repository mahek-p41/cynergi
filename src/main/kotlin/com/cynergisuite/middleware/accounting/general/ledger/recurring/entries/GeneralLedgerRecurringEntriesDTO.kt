package com.cynergisuite.middleware.accounting.general.ledger.recurring.entries

import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerRecurringEntries", title = "General Ledger Recurring Entries", description = "General ledger recurring entries entity")
data class GeneralLedgerRecurringEntriesDTO(

   @field:Valid
   @field:NotNull
   @field:Schema(description = "General ledger recurring", required = true)
   var generalLedgerRecurring: GeneralLedgerRecurringDTO? = null,

   @field:NotEmpty
   @field:Schema(description = "List of general ledger recurring distributions associated with the general ledger recurring", required = true)
   var generalLedgerRecurringDistributions: MutableList<GeneralLedgerRecurringDistributionDTO> = mutableListOf(),

   @field:NotNull
   @field:Schema(name = "entryDate", description = "Entry date must fall in general ledger recurring date range (for transfer only)", required = true)
   var entryDate: LocalDate? = null,

   ) {
   constructor(
      entity: GeneralLedgerRecurringEntriesEntity
   ) :
      this(
         generalLedgerRecurring = GeneralLedgerRecurringDTO(entity.generalLedgerRecurring),
         generalLedgerRecurringDistributions = entity.generalLedgerRecurringDistributions.map { GeneralLedgerRecurringDistributionDTO(it) } as MutableList<GeneralLedgerRecurringDistributionDTO>,
      )
}
