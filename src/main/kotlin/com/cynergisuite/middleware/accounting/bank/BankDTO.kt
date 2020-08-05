package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "Bank", title = "An entity containing a bank information", description = "An entity containing a bank information.")
data class BankDTO(

   @field:Positive
   var id: Long? = null,

   @field:NotNull
   @field:Schema(name = "name", description = "Human readable name for a bank.")
   var name: String? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "generalLedgerProfitCenter", required = true, description = "Store the bank is associated with.")
   var generalLedgerProfitCenter: SimpleIdentifiableDTO? = null,

   @field:Valid
   @field:Schema(name = "generalLedgerAccount", required = true, description = "Account the bank is associated with.")
   var generalLedgerAccount: SimpleIdentifiableDTO? = null

) : Identifiable {
   constructor(bankEntity: BankEntity) :
      this(
         id = bankEntity.id,
         name = bankEntity.name,
         generalLedgerProfitCenter = SimpleIdentifiableDTO(bankEntity.generalLedgerProfitCenter.myId()),
         generalLedgerAccount = SimpleIdentifiableDTO(bankEntity.generalLedgerAccount.myId())
      )

   override fun myId(): Long? = id
}
