package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.address.AddressValueObject
import com.cynergisuite.middleware.company.CompanyValueObject
import com.cynergisuite.middleware.store.StoreDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "BankCreateDTO", title = "An data transfer object containing a bank information", description = "An data transfer object containing a bank information.")
data class BankDTO (

   @field:Positive
   var id: Long? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "company", description = "Company that a division belong to.")
   var company: CompanyValueObject,

   @field:NotNull
   @field:Schema(name = "number", required = true, description = "The bank's number.")
   var number: Int,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "address", description = "Bank Address.")
   var address: AddressValueObject,

   @field:NotNull
   @field:Schema(name = "name", description = "Human readable name for a bank.")
   var name: String,

   @field:Schema(name = "generalLedgerProfitCenter", required = true, description = "Store the bank is associated with.")
   var generalLedgerProfitCenter: StoreDTO,

   @field:NotNull
   @field:Schema(name = "accountNumber", required = true, description = "The bank's account number.")
   var accountNumber: Int,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "currency", description = "The bank account's currency.")
   var currency: BankCurrencyType
   ) : Identifiable {

   override fun myId(): Long? = id
}
