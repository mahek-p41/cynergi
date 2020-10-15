package com.cynergisuite.middleware.accounting.general.ledger.control

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerControl", title = "General Ledger Control", description = "General ledger control")
data class GeneralLedgerControlDTO(

   @field:Positive
   @field:Schema(description = "General ledger control id")
   var id: Long? = null,

   @field:NotNull
   @field:Schema(description = "Period from date")
   var periodFrom: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Period to date")
   var periodTo: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Default profit center")
   var defaultProfitCenter: Int? = null,

   @field:Schema(nullable = true, description = "Default account payable account")
   var defaultAccountPayableAccount: SimpleIdentifiableDTO?,

   @field:Schema(nullable = true, description = "Default account payable discount account")
   var defaultAccountPayableDiscountAccount: SimpleIdentifiableDTO?,

   @field:Schema(nullable = true, description = "Default account receivable account")
   var defaultAccountReceivableAccount: SimpleIdentifiableDTO?,

   @field:Schema(nullable = true, description = "Default account receivable discount account")
   var defaultAccountReceivableDiscountAccount: SimpleIdentifiableDTO?,

   @field:Schema(nullable = true, description = "Default account misc inventory account")
   var defaultAccountMiscInventoryAccount: SimpleIdentifiableDTO?,

   @field:Schema(nullable = true, description = "Default account serialized inventory account")
   var defaultAccountSerializedInventoryAccount: SimpleIdentifiableDTO?,

   @field:Schema(nullable = true, description = "Default account unbilled inventory account")
   var defaultAccountUnbilledInventoryAccount: SimpleIdentifiableDTO?,

   @field:Schema(nullable = true, description = "Default account freight account")
   var defaultAccountFreightAccount: SimpleIdentifiableDTO?

) : Identifiable {
   constructor(
      entity: GeneralLedgerControlEntity
   ) :
      this(
         id = entity.id,
         periodFrom = entity.periodFrom,
         periodTo = entity.periodTo,
         defaultProfitCenter = entity.defaultProfitCenter,
         defaultAccountPayableAccount = entity.defaultAccountPayableAccount?.let { SimpleIdentifiableDTO(it) },
         defaultAccountPayableDiscountAccount = entity.defaultAccountPayableDiscountAccount?.let { SimpleIdentifiableDTO(it) },
         defaultAccountReceivableAccount = entity.defaultAccountReceivableAccount?.let { SimpleIdentifiableDTO(it) },
         defaultAccountReceivableDiscountAccount = entity.defaultAccountReceivableDiscountAccount?.let { SimpleIdentifiableDTO(it) },
         defaultAccountMiscInventoryAccount = entity.defaultAccountMiscInventoryAccount?.let { SimpleIdentifiableDTO(it) },
         defaultAccountSerializedInventoryAccount = entity.defaultAccountSerializedInventoryAccount?.let { SimpleIdentifiableDTO(it) },
         defaultAccountUnbilledInventoryAccount = entity.defaultAccountUnbilledInventoryAccount?.let { SimpleIdentifiableDTO(it) },
         defaultAccountFreightAccount = entity.defaultAccountFreightAccount?.let { SimpleIdentifiableDTO(it) }
      )

   override fun myId(): Long? = id
}
