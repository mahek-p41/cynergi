package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.store.StoreDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerRecurringDistribution", title = "General Ledger Recurring Distribution", description = "General ledger recurring distribution entity")
data class GeneralLedgerRecurringDistributionDTO(

   @field:Schema(description = "General ledger recurring distribution id")
   var id: UUID? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(description = "General ledger recurring")
   var generalLedgerRecurring: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(description = "General ledger distribution account")
   var generalLedgerDistributionAccount: AccountDTO? = null,

   @field:NotNull
   @field:Schema(description = "General ledger distribution profit center")
   var generalLedgerDistributionProfitCenter: StoreDTO? = null,

   @field:NotNull
   @field:Schema(description = "General ledger distribution amount")
   var generalLedgerDistributionAmount: BigDecimal? = null

) : Identifiable {
   constructor(
      entity: GeneralLedgerRecurringDistributionEntity
   ) :
      this(
         id = entity.id,
         generalLedgerRecurring = SimpleIdentifiableDTO(entity.generalLedgerRecurringId),
         generalLedgerDistributionAccount = AccountDTO(entity.generalLedgerDistributionAccount),
         generalLedgerDistributionProfitCenter = StoreDTO(entity.generalLedgerDistributionProfitCenter),
         generalLedgerDistributionAmount = entity.generalLedgerDistributionAmount
      )

   override fun myId(): UUID? = id
}
