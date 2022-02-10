package com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerReversalDistribution", title = "General Ledger Reversal Distribution", description = "General ledger reversal distribution entity")
data class GeneralLedgerReversalDistributionDTO(

   @field:Schema(description = "General ledger reversal distribution id")
   var id: UUID? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(description = "General ledger reversal")
   var generalLedgerReversal: SimpleIdentifiableDTO? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(description = "General ledger reversal distribution account")
   var generalLedgerReversalDistributionAccount: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(description = "General ledger reversal distribution profit center")
   var generalLedgerReversalDistributionProfitCenter: SimpleLegacyIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(description = "General ledger reversal distribution amount")
   var generalLedgerReversalDistributionAmount: BigDecimal? = null

) : Identifiable {
   constructor(
      entity: GeneralLedgerReversalDistributionEntity
   ) :
      this(
         id = entity.id,
         generalLedgerReversal = SimpleIdentifiableDTO(entity.generalLedgerReversal),
         generalLedgerReversalDistributionAccount = SimpleIdentifiableDTO(entity.generalLedgerReversalDistributionAccount),
         generalLedgerReversalDistributionProfitCenter = SimpleLegacyIdentifiableDTO(entity.generalLedgerReversalDistributionProfitCenter.myId()),
         generalLedgerReversalDistributionAmount = entity.generalLedgerReversalDistributionAmount
      )

   override fun myId(): UUID? = id
}
