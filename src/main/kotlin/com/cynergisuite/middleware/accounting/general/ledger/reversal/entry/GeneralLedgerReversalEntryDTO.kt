package com.cynergisuite.middleware.accounting.general.ledger.reversal.entry

import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalDTO
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.GeneralLedgerReversalDistributionDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerReversalEntry", title = "General Ledger Reversal Entry", description = "General ledger reversal entry entity")
data class GeneralLedgerReversalEntryDTO(

   @field:Valid
   @field:NotNull
   @field:Schema(description = "General ledger reversal")
   var generalLedgerReversal: GeneralLedgerReversalDTO? = null,

   @field:NotEmpty
   @field:Schema(description = "List of general ledger reversal distributions associated with the general ledger reversal")
   var generalLedgerReversalDistributions: MutableList<GeneralLedgerReversalDistributionDTO> = mutableListOf(),

   @field:NotNull
   @field:Schema(description = "General ledger distribution amount net balance (should be 0 for records to be posted/updated)")
   var balance: BigDecimal? = null

) {
   constructor(
      entity: GeneralLedgerReversalEntryEntity
   ) :
      this(
         generalLedgerReversal = GeneralLedgerReversalDTO(entity.generalLedgerReversal),
         generalLedgerReversalDistributions = entity.generalLedgerReversalDistributions.map { GeneralLedgerReversalDistributionDTO(it) } as MutableList<GeneralLedgerReversalDistributionDTO>,
         balance = BigDecimal.ZERO
      )
}
