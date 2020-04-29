package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.middleware.address.AddressValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "BankDTO", title = "An entity containing a bank information", description = "An entity containing a bank information.")
data class BankDTO (

   @field:Positive
   var id: Long? = null,

   @field:Positive
   var number: Int? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "address", description = "Bank Address.")
   var address: AddressValueObject,

   @field:NotNull
   @field:Schema(name = "name", description = "Human readable name for a bank.")
   var name: String,

   @field:Valid
   @field:Schema(name = "generalLedgerProfitCenter", required = true, description = "Store the bank is associated with.")
   var generalLedgerProfitCenter: SimpleIdentifiableDataTransferObject,

   @field:NotNull
   @field:Schema(name = "accountNumber", required = true, description = "The bank's account number.")
   var accountNumber: Int,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "currency", description = "The bank account's currency.")
   var currency: BankCurrencyTypeValueObject
   ) : Identifiable {
   constructor(bankEntity: BankEntity) :
      this(
         id = bankEntity.id,
         number = bankEntity.number,
         address = AddressValueObject(bankEntity.address),
         name = bankEntity.name,
         generalLedgerProfitCenter = SimpleIdentifiableDataTransferObject(bankEntity.generalLedgerProfitCenter.myId()),
         accountNumber = bankEntity.accountNumber,
         currency = BankCurrencyTypeValueObject(bankEntity.currency)
      )

   override fun myId(): Long? = id
}
