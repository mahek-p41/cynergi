package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "General Ledger Account Posting DTO", title = "DTO for transferring GL Details AND GL Journal Entries", description = "DTO for transferring GL Details AND GL Journal Entries")
class GeneralLedgerAccountPostingDTO (
   @field:NotNull
   @field:Schema(description = "General Ledger Distribution", required = true)
   var glDetail: GeneralLedgerDetailDTO? = null,

   @field:Schema(name = "bankType", description = "Bank reconciliation type", required = false)
   var bankType: BankReconciliationTypeDTO? = null,
)
