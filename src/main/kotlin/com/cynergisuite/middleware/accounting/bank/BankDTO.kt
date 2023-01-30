package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.store.StoreDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "Bank", title = "An entity containing a bank information", description = "An entity containing a bank information.")
data class BankDTO(

   var id: UUID? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "number", description = "Bank number")
   var number: Long? = null,

   @field:NotNull
   @field:Schema(name = "name", description = "Human readable name for a bank.")
   var name: String? = null,

   @field:NotNull
   @field:Schema(name = "generalLedgerProfitCenter", required = true, description = "Store the bank is associated with.")
   var generalLedgerProfitCenter: StoreDTO? = null,

   @field:NotNull
   @field:Schema(name = "generalLedgerAccount", required = true, description = "Account the bank is associated with.")
   var generalLedgerAccount: AccountDTO? = null

) : Identifiable {
   constructor(bankEntity: BankEntity) :
      this(
         id = bankEntity.id,
         number = bankEntity.number,
         name = bankEntity.name,
         generalLedgerProfitCenter = StoreDTO(bankEntity.generalLedgerProfitCenter),
         generalLedgerAccount = AccountDTO(bankEntity.generalLedgerAccount)
      )

   override fun myId(): UUID? = id
}
