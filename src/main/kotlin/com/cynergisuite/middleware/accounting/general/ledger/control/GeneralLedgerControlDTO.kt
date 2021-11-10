package com.cynergisuite.middleware.accounting.general.ledger.control

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerControl", title = "General Ledger Control", description = "General ledger control")
data class GeneralLedgerControlDTO(

   @field:Schema(description = "General ledger control id")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Default profit center")
   var defaultProfitCenter: SimpleLegacyIdentifiableDTO? = null,

   @field:Schema(nullable = true, description = "Default account payable account")
   var defaultAccountPayableAccount: AccountDTO? = null,

   @field:Schema(nullable = true, description = "Default account payable discount account")
   var defaultAccountPayableDiscountAccount: AccountDTO? = null,

   @field:Schema(nullable = true, description = "Default account receivable account")
   var defaultAccountReceivableAccount: AccountDTO? = null,

   @field:Schema(nullable = true, description = "Default account receivable discount account")
   var defaultAccountReceivableDiscountAccount: AccountDTO? = null,

   @field:Schema(nullable = true, description = "Default account misc inventory account")
   var defaultAccountMiscInventoryAccount: AccountDTO? = null,

   @field:Schema(nullable = true, description = "Default account serialized inventory account")
   var defaultAccountSerializedInventoryAccount: AccountDTO? = null,

   @field:Schema(nullable = true, description = "Default account unbilled inventory account")
   var defaultAccountUnbilledInventoryAccount: AccountDTO? = null,

   @field:Schema(nullable = true, description = "Default account freight account")
   var defaultAccountFreightAccount: AccountDTO? = null

) : Identifiable {
   constructor(
      entity: GeneralLedgerControlEntity
   ) :
      this(
         id = entity.id,
         defaultProfitCenter = SimpleLegacyIdentifiableDTO(entity.defaultProfitCenter.myId()),
         defaultAccountPayableAccount = entity.defaultAccountPayableAccount?.let { AccountDTO(it) },
         defaultAccountPayableDiscountAccount = entity.defaultAccountPayableDiscountAccount?.let { AccountDTO(it) },
         defaultAccountReceivableAccount = entity.defaultAccountReceivableAccount?.let { AccountDTO(it) },
         defaultAccountReceivableDiscountAccount = entity.defaultAccountReceivableDiscountAccount?.let { AccountDTO(it) },
         defaultAccountMiscInventoryAccount = entity.defaultAccountMiscInventoryAccount?.let { AccountDTO(it) },
         defaultAccountSerializedInventoryAccount = entity.defaultAccountSerializedInventoryAccount?.let { AccountDTO(it) },
         defaultAccountUnbilledInventoryAccount = entity.defaultAccountUnbilledInventoryAccount?.let { AccountDTO(it) },
         defaultAccountFreightAccount = entity.defaultAccountFreightAccount?.let { AccountDTO(it) }
      )

   override fun myId(): UUID? = id
}
