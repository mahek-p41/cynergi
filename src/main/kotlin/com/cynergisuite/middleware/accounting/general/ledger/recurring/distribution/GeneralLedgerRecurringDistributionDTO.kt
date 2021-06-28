package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerRecurringDistribution", title = "General Ledger Recurring Distribution", description = "General ledger recurring distribution entity")
data class GeneralLedgerRecurringDistributionDTO(

   @field:Positive
   @field:Schema(description = "General ledger recurring distribution id")
   var id: Long? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(description = "General ledger recurring")
   var generalLedgerRecurring: GeneralLedgerRecurringDTO? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(description = "General ledger distribution account")
   var generalLedgerDistributionAccount: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(description = "General ledger distribution profit center")
   var generalLedgerDistributionProfitCenter: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(description = "General ledger distribution amount")
   var generalLedgerDistributionAmount: BigDecimal? = null

) : Identifiable {
   constructor(
      entity: GeneralLedgerRecurringDistributionEntity
   ) :
      this(
         id = entity.id,
         generalLedgerRecurring = GeneralLedgerRecurringDTO(entity.generalLedgerRecurring),
         generalLedgerDistributionAccount = SimpleIdentifiableDTO(entity.generalLedgerDistributionAccount),
         generalLedgerDistributionProfitCenter = SimpleIdentifiableDTO(entity.generalLedgerDistributionProfitCenter),
         generalLedgerDistributionAmount = entity.generalLedgerDistributionAmount
      )

   override fun myId(): Long? = id
}
